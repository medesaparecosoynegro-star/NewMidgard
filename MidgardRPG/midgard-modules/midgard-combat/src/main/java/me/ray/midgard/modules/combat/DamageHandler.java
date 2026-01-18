package me.ray.midgard.modules.combat;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.debug.DebugCategory;
import me.ray.midgard.core.debug.MidgardLogger;
import me.ray.midgard.core.attribute.AttributeInstance;
import me.ray.midgard.core.attribute.CoreAttributeData;
import me.ray.midgard.core.integration.MythicMobsIntegration;
import me.ray.midgard.core.profile.MidgardProfile;
import me.ray.midgard.modules.combat.mechanics.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manipulador central de cálculo e aplicação de dano.
 * <p>
 * Esta classe orquestra todo o fluxo de dano do RPG, incluindo:
 * <ul>
 *     <li>Identificação do tipo de ataque (Físico, Mágico, Projétil).</li>
 *     <li>Cálculo de dano base e escalonamento por atributos.</li>
 *     <li>Aplicação de mecânicas defensivas (Esquiva, Bloqueio, Aparar).</li>
 *     <li>Cálculo de mitigação de dano (Armadura, Resistência Mágica).</li>
 *     <li>Aplicação de efeitos pós-dano (Roubo de Vida, Espinhos).</li>
 * </ul>
 */
public class DamageHandler {

    private final CombatManager combatManager;
    private final CombatConfig config;
    private final DamageIndicatorManager indicatorManager;

    // Mechanics & Calculators
    private final PhysicalDamageCalculator physicalCalculator;
    private final MagicalDamageCalculator magicalCalculator;
    private final ElementalDamageCalculator elementalCalculator;
    private final MitigationHandler mitigationHandler;
    private final DodgeMechanic dodgeMechanic;
    private final ParryMechanic parryMechanic;
    private final BlockMechanic blockMechanic;
    private final ThornsMechanic thornsMechanic;
    private final LifeStealMechanic lifeStealMechanic;

    /**
     * Construtor do DamageHandler.
     *
     * @param combatManager Gerenciador de combate.
     * @param config Configuração de combate.
     * @param indicatorManager Gerenciador de indicadores de dano.
     */
    public DamageHandler(CombatManager combatManager, CombatConfig config, DamageIndicatorManager indicatorManager) {
        this.combatManager = combatManager;
        this.config = config;
        this.indicatorManager = indicatorManager;

        // Initialize components
        this.physicalCalculator = new PhysicalDamageCalculator(combatManager);
        this.magicalCalculator = new MagicalDamageCalculator();
        this.elementalCalculator = new ElementalDamageCalculator(config);
        this.mitigationHandler = new MitigationHandler(config);
        this.dodgeMechanic = new DodgeMechanic(indicatorManager);
        this.parryMechanic = new ParryMechanic(indicatorManager);
        this.blockMechanic = new BlockMechanic(indicatorManager);
        this.thornsMechanic = new ThornsMechanic();
        this.lifeStealMechanic = new LifeStealMechanic(combatManager);
    }

    /**
     * Processa um evento de dano do Bukkit.
     *
     * @param event O evento de dano original.
     */
    public void handleDamage(EntityDamageEvent event) {
        try {
            if (!(event.getEntity() instanceof LivingEntity victim)) return;

            RPGDamageContext context = new RPGDamageContext(event);

            double damage = event.getDamage(); // Começa com o dano vanilla
            String forcedElement = DamageOverrideContext.getForcedElement(victim);
            String forcedType = DamageOverrideContext.getForcedType(victim);
            Double forcedDamage = DamageOverrideContext.getForcedDamage(victim);

            if (forcedDamage != null) {
                damage = forcedDamage;
                // Resetar o dano do evento para o valor forçado para consistência visual/outros plugins
                // event.setDamage(damage); 
            }

            // Debug Inicial para rastrear valores brutos
            MidgardLogger.debug(DebugCategory.COMBAT, "Calculando Dano: %s -> %s (Base: %.2f | Forçado: %.2f | Tipo: %s | Elem: %s)",
                    event.getDamage(), forcedDamage != null ? forcedDamage : 0.0,
                    forcedType != null ? forcedType : "VANILLA", forcedElement != null ? forcedElement : "NENHUM");

            double elementalDamage = 0.0;
            double lifeSteal = 0.0;
            boolean isCritical = false;
            Player attackerPlayer = null;
            LivingEntity attackerEntity = null;
            CoreAttributeData attackerAttributes = null;
            int attackerLevel = 1;
            Map<String, Double> damageMap = new LinkedHashMap<>();
            String mainDamageKey = null;

            // Identificação do Atacante
            if (event instanceof EntityDamageByEntityEvent damageByEntityEvent) {
                Entity damager = damageByEntityEvent.getDamager();
                if (damager instanceof Player p) {
                    attackerPlayer = p;
                    attackerEntity = p;
                } else if (damager instanceof Projectile proj && proj.getShooter() instanceof LivingEntity shooter) {
                    attackerEntity = shooter;
                    if (shooter instanceof Player p) attackerPlayer = p;
                } else if (damager instanceof LivingEntity le) {
                    attackerEntity = le;
                }

                if (attackerEntity != null) {
                    attackerAttributes = getEntityAttributes(attackerEntity);
                    
                    if (attackerAttributes != null) {
                        if (attackerPlayer != null) {
                            MidgardProfile attackerProfile = MidgardCore.getProfileManager().getProfile(attackerPlayer.getUniqueId());
                            if (attackerProfile != null) {
                                CombatData attackerCombatData = attackerProfile.getOrCreateData(CombatData.class);
                                attackerLevel = attackerCombatData.getLevel();
                            }
                        } else {
                            // Mob Level? Could read from tags too if needed. Default 1.
                        }

                        // Extração de atributos básicos do atacante
                        AttributeInstance lifeStealAttr = attackerAttributes.getInstance(CombatAttributes.LIFE_STEAL);
                        lifeSteal = lifeStealAttr != null ? lifeStealAttr.getValue() : 0.0;

                        AttributeInstance spellVampAttr = attackerAttributes.getInstance(CombatAttributes.SPELL_VAMPIRISM);
                        double spellVamp = spellVampAttr != null ? spellVampAttr.getValue() : 0.0;

                        // Lógica de Tipped Arrow
                        boolean isTippedArrow = false;
                        if (damager instanceof org.bukkit.entity.Arrow arrow) {
                            if (arrow.hasCustomEffects()) {
                                isTippedArrow = true;
                            } else {
                                try {
                                    @SuppressWarnings("deprecation")
                                    boolean hasEffect = arrow.getBasePotionType().getEffectType() != null;
                                    if (hasEffect) {
                                        isTippedArrow = true;
                                    }
                                } catch (Throwable t) {
                                    combatManager.getPlugin().getLogger().warning("Erro ao verificar tipo de poção da flecha: " + t.getMessage());
                                }
                            }
                        }

                        // Decisão de qual calculadora usar
                        boolean isPhysicalAttack = !isTippedArrow && (context.hasCategory(RPGDamageCategory.PHYSICAL) ||
                                (context.hasCategory(RPGDamageCategory.PROJECTILE) && !context.hasCategory(RPGDamageCategory.MAGICAL)));

                        DamageResult result;
                        // forcedElement já foi obtido no início do método
                        
                        if (forcedElement != null) {
                            MidgardLogger.debug(DebugCategory.COMBAT, "Elemento forçado detectado: %s", forcedElement);
                        }

                        // Se não houver elemento forçado via código, verifica tags no causador do dano (MythicMobs/Plugins)
                        if (forcedElement == null) {
                            for (String tag : damager.getScoreboardTags()) {
                                if (tag.toLowerCase().startsWith("midgard.damage.")) {
                                    forcedElement = tag.substring("midgard.damage.".length());
                                    break;
                                }
                            }
                        }

                        // Check for forced type (MythicMobs)
                        forcedType = DamageOverrideContext.getForcedType(victim);
                        if (forcedType != null) {
                             try {
                                 RPGDamageCategory cat = RPGDamageCategory.valueOf(forcedType.toUpperCase());
                                 context.addCategory(cat);
                                 if (cat == RPGDamageCategory.MAGICAL) isPhysicalAttack = false;
                             } catch (IllegalArgumentException ignored) {}
                        }

                        if (forcedElement != null) {
                            double mitigated = elementalCalculator.calculateMitigatedDamage(forcedElement, damage, victim);
                            elementalDamage += mitigated;
                            damageMap.put(forcedElement.toLowerCase() + "_damage", mitigated);
                            result = new DamageResult(0, false, "Elemental");
                        } else if (attackerPlayer == null) {
                            // MOB LOGIC: Trust the damage from MythicMobs/Vanilla, just pass it through.
                            // Mitigation will happen later in handleDamage.
                            String typeLabel = isPhysicalAttack ? "Physical" : "Magical";
                            if (context.hasCategory(RPGDamageCategory.GLOBAL)) typeLabel = "True";
                            
                            result = new DamageResult(damage, false, typeLabel);
                        } else if (isPhysicalAttack) {
                            // Player Physical Logic
                            result = physicalCalculator.calculate(attackerPlayer, victim, attackerAttributes, context, damage);
                        } else if (context.hasCategory(RPGDamageCategory.MAGICAL) || isTippedArrow) {
                            result = magicalCalculator.calculate(attackerPlayer, victim, attackerAttributes, context, damage);
                            
                            if (spellVamp > 0) {
                                lifeSteal += spellVamp;
                            }
                            
                            // Ajuste para AoE se necessário
                            boolean isAoE = context.hasCategory(RPGDamageCategory.AOE);
                            if (!isAoE) {
                                Entity subDamager = damageByEntityEvent.getDamager();
                                if (subDamager.getType().name().contains("POTION") || subDamager.getType().name().contains("AREA_EFFECT")) {
                                }
                            }
                        } else {
                            // Fallback
                            List<String> types = new ArrayList<>();
                            if (context.hasCategory(RPGDamageCategory.PROJECTILE)) types.add("Projectile");
                            if (context.hasCategory(RPGDamageCategory.PHYSICAL)) types.add("Physical");
                            if (types.isEmpty()) types.add("Physical");
                            result = new DamageResult(damage, false, String.join("+", types));
                        }

                        damage = result.getDamage();
                        isCritical = result.isCritical();
                        mainDamageKey = result.getDamageKey();
                        damageMap.put(mainDamageKey, damage);

                        // Cálculo Elemental
                        double[] totalElemental = {elementalDamage};
                        elementalCalculator.calculateAndApply(attackerAttributes, victim, damageMap, totalElemental);
                        elementalDamage = totalElemental[0];
                        
                        // Mensagens de dano elemental removidas - feedback visual via hologramas/indicators

                        // Combat Tag
                        if (attackerPlayer != null) {
                            UUID attackerId = attackerPlayer.getUniqueId();
                            boolean wasInCombat = combatManager.getCombatTag().containsKey(attackerId) && 
                                                  combatManager.getCombatTag().get(attackerId) > System.currentTimeMillis();
                            combatManager.updateCombatTag(attackerId);
                            
                            // Send combat mode message only if wasn't already in combat
                            if (!wasInCombat && CombatModule.getInstance() != null) {
                                String combatMsg = CombatModule.getInstance().getMessage("combat_mode.enabled");
                                me.ray.midgard.core.text.MessageUtils.send(attackerPlayer, combatMsg);
                            }
                        }
                    }
                }
            }

            // Se não houve cálculo de atacante (dano ambiental, mob sem atributos, etc), categoriza o básico
            if (damageMap.isEmpty()) {
                List<String> types = new ArrayList<>();
                boolean isProjectile = context.hasCategory(RPGDamageCategory.PROJECTILE);
                if (isProjectile) types.add("Projectile");
                if (context.hasCategory(RPGDamageCategory.PHYSICAL)) types.add("Physical");
                if (context.hasCategory(RPGDamageCategory.MAGICAL)) types.add("Magical");
                if (context.hasCategory(RPGDamageCategory.ENVIRONMENTAL)) types.add("Environment");
                if (context.hasCategory(RPGDamageCategory.GLOBAL)) types.add("True");
                if (types.isEmpty()) types.add("Physical");

                String key = String.join("+", types);
                mainDamageKey = key;
                damageMap.put(key, damage);
            }

            // --- Processamento na Vítima (Unificado para Player e Mob) ---
            CoreAttributeData victimAttributes = getEntityAttributes(victim);
            
            // Mecânicas Defensivas (Dodge/Parry/Block)
            // Agora suporta Mobs também, pois os atributos são carregados corretamente via MythicMobsIntegration
            if (dodgeMechanic.apply(event, victim, victimAttributes, attackerAttributes)) return;
            if (parryMechanic.apply(event, victim, victimAttributes)) return;
            
            damage = blockMechanic.apply(damage, victim, victimAttributes);

            // Thorns
            if (attackerPlayer != null) {
                thornsMechanic.apply(attackerPlayer, damage, elementalDamage, victimAttributes, context);
            }

            // Mitigação (Defesa, Resistência, Reduções)
            damage = mitigationHandler.applyMitigation(damage, victimAttributes, attackerAttributes, attackerLevel, context, event.getCause(), attackerPlayer != null);

            // Atualiza mapa com dano mitigado
            if (mainDamageKey != null) {
                damageMap.put(mainDamageKey, damage);
            }

            // Adiciona Elemental (já mitigado)
            damage += elementalDamage;

            // Debug Log
            if (victim instanceof Player p && combatManager.isDebugging(p.getUniqueId())) {
                // Update Scoreboard
                String attackerName = (attackerEntity != null ? attackerEntity.getName() : "Ambiente");
                String causeName = event.getCause().name();
                String cats = context.getCategories().toString();
                String dmgStr = String.format("%.2f", damage);
                String elemStr = String.format("%.2f", elementalDamage);
                String forcedStr = (forcedType != null ? forcedType : "") + (forcedElement != null ? " " + forcedElement : "");
                
                combatManager.getDebugScoreboard().update(p, attackerName, causeName, cats, dmgStr, elemStr, forcedStr, damageMap);
                
                // Chat Log (Opcional - Comentado para evitar spam se o scoreboard estiver ativo)
                /*
                p.sendMessage("§8§m--------------------------------");
                p.sendMessage("§e§l[DEBUG COMBATE - RECEBIDO]");
                // ...
                */
            }
            if (attackerPlayer != null && combatManager.isDebugging(attackerPlayer.getUniqueId())) {
                attackerPlayer.sendMessage("§e[Debug] §fCausado: §c" + String.format("%.2f", damage) + " §7em " + victim.getName());
            }

            // Aplica Dano Final
            if (victim instanceof Player player) {
                MidgardProfile profile = MidgardCore.getProfileManager().getProfile(player.getUniqueId());
                if (profile != null) {
                    CombatData combatData = profile.getOrCreateData(CombatData.class);
                    AttributeInstance maxHealthAttr = victimAttributes.getInstance(CombatAttributes.MAX_HEALTH);
                    double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 100;

                    double oldHealth = combatData.getCurrentHealth();
                    double newHealth = oldHealth - damage;
                    combatData.setCurrentHealth(newHealth);
                    
                    if (combatManager.isDebugging(player.getUniqueId())) {
                        player.sendMessage("§e[Debug Vida] §fAnterior: §a" + String.format("%.2f", oldHealth) + " §f-> Nova: §c" + String.format("%.2f", newHealth) + " §7(Max: " + maxHealth + ")");
                    }

                    // Life Steal
                    if (attackerPlayer != null) {
                        lifeStealMechanic.apply(attackerPlayer, damage, lifeSteal);
                    }

                    // Combat Tag Vítima
                    combatManager.updateCombatTag(player.getUniqueId());

                    // Sincroniza Vida
                    combatManager.syncHealth(player, newHealth, maxHealth);

                    event.setDamage(0); // Previne dano vanilla

                    if (newHealth <= 0) {
                        player.setHealth(0);
                    }
                }
            } else {
                // Vítima é Mob
                if (attackerPlayer != null) {
                    lifeStealMechanic.apply(attackerPlayer, damage, lifeSteal);
                }
                
                // Sanitize damage to prevent crashes
                if (Double.isNaN(damage) || Double.isInfinite(damage)) {
                    MidgardLogger.warn("Dano inválido detectado via verificação NaN/Infinity. Resetando para 0. Vítima: " + victim.getName());
                    damage = 0.0;
                }
                if (damage < 0) damage = 0;
                
                event.setDamage(damage);
            }

            // Indicadores
            if (config != null && config.indicatorEnabled && damage > 0.05) {
                indicatorManager.spawnIndicator(victim, damageMap, isCritical);
            }

        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Erro no evento onDamage", e);
        }
    }

    private String getDominantElement(Map<String, Double> damageMap) {
        String dominant = null;
        double maxDmg = 0;
        for (Map.Entry<String, Double> entry : damageMap.entrySet()) {
            if (entry.getKey().contains("_damage") && entry.getValue() > maxDmg) {
                maxDmg = entry.getValue();
                dominant = entry.getKey();
            }
        }
        return dominant;
    }
    
    private String formatElementName(String elementKey) {
        if (elementKey == null) return "";
        String name = elementKey.replace("_damage", "");
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
    
    private CoreAttributeData getEntityAttributes(LivingEntity entity) {
        if (entity instanceof Player player) {
            MidgardProfile profile = MidgardCore.getProfileManager().getProfile(player.getUniqueId());
            if (profile != null) {
                return profile.getOrCreateData(CoreAttributeData.class);
            }
        } else {
            // Mob - Create temporary attribute data from tags
            CoreAttributeData data = new CoreAttributeData();
            Map<String, Double> attributes = MythicMobsIntegration.getAttributes(entity);
            
            for (Map.Entry<String, Double> entry : attributes.entrySet()) {
                AttributeInstance instance = data.getInstance(entry.getKey());
                if (instance != null) {
                    instance.setBaseValue(entry.getValue());
                }
            }
            return data;
        }
        return new CoreAttributeData(); // Empty fallback
    }
}