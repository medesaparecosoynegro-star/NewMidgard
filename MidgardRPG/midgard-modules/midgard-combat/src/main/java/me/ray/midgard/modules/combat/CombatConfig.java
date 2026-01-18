package me.ray.midgard.modules.combat;

import me.ray.midgard.core.config.ConfigWrapper;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe de configuração para o módulo de combate.
 * <p>
 * Carrega e armazena as configurações definidas no arquivo 'modules/combat.yml'.
 * Inclui configurações para indicadores de dano, combat tag, stamina e sistema de níveis.
 */
public class CombatConfig {

    private final JavaPlugin plugin;
    private final ConfigWrapper configWrapper;
    private FileConfiguration config;

    // Settings
    /** Se os indicadores de dano (hologramas) estão ativados. */
    public boolean indicatorEnabled;
    /** Duração em ticks que o indicador de dano permanece visível. */
    public int indicatorDuration;
    /** Formato de cor/estilo para dano normal. */
    public String indicatorFormatNormal;
    /** Formato de cor/estilo para dano físico. */
    public String indicatorFormatPhysical; // Novo
    /** Formato de cor/estilo para dano crítico. */
    public String indicatorFormatCritical;
    /** Ícone exibido para dano crítico. */
    public String indicatorIconCritical;
    /** Ícone exibido para quando o jogador usa uma arma. */
    public String indicatorIconWeapon;
    /** Ícone exibido para dano físico. */
    public String indicatorIconPhysical; // Novo
    /** Ícone exibido para dano de projétil. */
    public String indicatorIconProjectile;
    /** Ícone exibido para dano mágico. */
    public String indicatorIconMagical;
    /** Ícone exibido para dano ambiental. */
    public String indicatorIconEnvironment;
    /** Ícone exibido para dano verdadeiro. */
    public String indicatorIconTrue;
    /** Ícone exibido para dano em área (AoE). */
    public String indicatorIconAoE;
    
    /** Formato de cor para dano mágico. */
    public String indicatorFormatMagical;
    /** Formato de cor para dano de projétil. */
    public String indicatorFormatProjectile;
    /** Formato de cor para dano ambiental. */
    public String indicatorFormatEnvironment;
    /** Formato de cor para dano verdadeiro. */
    public String indicatorFormatTrue;
    /** Formato de cor para dano em área (AoE). */
    public String indicatorFormatAoE;

    /** Cor de fundo do indicador de dano (formato ARGB Hex). */
    public String indicatorBackgroundColor;
    /** Template da mensagem do indicador. Suporta %icons%, %color%, %damage%. */
    public String indicatorTemplate; // Novo: "%icons% %color%%damage%"
    /** Número de casas decimais para exibir no valor do dano. */
    public int indicatorDecimals; // Novo
    /** Gravidade aplicada ao movimento do indicador. */
    public double indicatorGravity; // Novo
    /** Velocidade inicial vertical do indicador. */
    public double indicatorInitialVelocity; // Novo
    
    /** Mapas de formatos e ícones para danos elementais. */
    public Map<String, String> elementalFormats = new HashMap<>();
    public Map<String, String> elementalIcons = new HashMap<>();
    
    /** Duração do estado de combate em segundos. */
    public long combatTagDuration;
    
    /** Quantidade de stamina drenada por verificação ao correr. */
    public double staminaSprintDrain;
    /** Intervalo em ticks entre verificações de stamina ao correr. */
    public long staminaCheckInterval;
    
    /** Divisor usado na fórmula de defesa para cálculo de redução de dano. */
    public double defenseDivisor;
    /** Se a defesa escala com o nível do atacante. */
    public boolean defenseScalingEnabled;
    /** Divisor base para o escalonamento de defesa. */
    public double defenseScalingBase;
    /** Limite máximo de mitigação de dano (0.0 a 1.0). */
    public double maxMitigation;

    // Level System (UPDATED)
    /** Nível máximo alcançável. */
    public int maxLevel;
    
    // Requirements
    public double xpRequirementsBase;
    public double xpRequirementsLinear;
    public double xpRequirementsExponential;
    
    // XP Gain - Core
    public double xpGainDefaultBase;
    
    // XP Gain - Variance (RNG)
    public boolean xpVarianceEnabled;
    public double xpVarianceMin;
    public double xpVarianceMax;
    
    // XP Gain - Scaling
    public boolean xpScalingMobLevelImpact;
    public double xpScalingMobLevelMultiplier;
    
    // XP Gain - Disparity
    public boolean xpDisparityEnabled;
    
    public int xpPenaltyThreshold;
    public double xpPenaltyReduction;
    public double xpPenaltyMinCap;
    
    public int xpBonusThreshold;
    public double xpBonusIncrement;
    public double xpBonusMaxCap;

    // Attribute Scaling
    public double strengthToPhysicalDamage;
    public double intelligenceToMana;
    public double intelligenceToMagicDamage;
    public double intelligenceToManaRegen;
    public double dexterityToCritChance;
    public double agilityToSpeed;
    public double agilityToDodge;
    public double vitalityToHealth;
    public double vitalityToDefense;
    
    /** Multiplicadores de dano elemental (Atacante -> Vítima -> Multiplicador). */
    public Map<String, Map<String, Double>> elementalMultipliers = new HashMap<>();
    
    /** Se as interações elementais (fraquezas/resistências) estão ativadas. */
    public boolean elementalInteractionsEnabled;

    // Formula Settings
    public enum ScalingMode { ADDITIVE, MULTIPLICATIVE }
    public ScalingMode damageFormulaMode;
    public double baseHandDamage;
    public double strengthMultiplier;   // % damage per strength point (Multiplicative mode)
    public double intelligenceMultiplier; // % damage per int point (Multiplicative mode)

    /**
     * Construtor da configuração de combate.
     * Carrega o arquivo de configuração.
     *
     * @param plugin Instância do plugin principal.
     */
    public CombatConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configWrapper = new ConfigWrapper(plugin, "modules/combat/config.yml");
        reload();
    }

    /**
     * Recarrega as configurações do arquivo.
     * Atualiza os valores em memória.
     */
    public void reload() {
        this.config = configWrapper.getConfig();
        loadDefaults();
        loadValues();
        // NÃO salve a config aqui, ou os comentários serão perdidos!
        // configWrapper.saveConfig(); 
    }

    /**
     * Define os valores padrão para a configuração caso não existam.
     */
    private void loadDefaults() {
        // Padrões correspondem ao arquivo resources/modules/combat.yml
        config.addDefault("damage-indicator.enabled", true);
        config.addDefault("damage-indicator.duration-ticks", 18);
        config.addDefault("damage-indicator.format.normal", "§7");
        config.addDefault("damage-indicator.format.physical", "§f"); // Branco para físico
        config.addDefault("damage-indicator.format.critical", "§c§l");
        config.addDefault("damage-indicator.icon.critical", "⚔"); // Apenas a espada
        config.addDefault("damage-indicator.icon.weapon", "🗡 "); // Ícone de arma
        config.addDefault("damage-indicator.icon.physical", "✘ "); // Ícone físico (soco/impacto)
        config.addDefault("damage-indicator.icon.projectile", "🏹 ");
        config.addDefault("damage-indicator.icon.magical", "✩ ");
        config.addDefault("damage-indicator.icon.environment", "🌲 ");
        config.addDefault("damage-indicator.icon.true", "🛡 ");
        config.addDefault("damage-indicator.icon.aoe", "☀ "); // Ícone de sol para AoE
        
        config.addDefault("damage-indicator.format.magical", "§d"); // Roxo Claro
        config.addDefault("damage-indicator.format.projectile", "§7"); // Cinza (igual físico por padrão)
        config.addDefault("damage-indicator.format.environment", "§2"); // Verde Escuro
        config.addDefault("damage-indicator.format.true", "§f"); // Branco
        config.addDefault("damage-indicator.format.aoe", "§e"); // Amarelo para AoE

        config.addDefault("damage-indicator.background-color", "#80000000"); // 50% Preto
        config.addDefault("damage-indicator.template", "%icons% %color%%damage%");
        config.addDefault("damage-indicator.decimals", 1);
        config.addDefault("damage-indicator.gravity", 0.04);
        config.addDefault("damage-indicator.initial-velocity", 0.25);
        
        config.addDefault("damage-indicator.format.fire", "§c");
        config.addDefault("damage-indicator.format.ice", "§b");
        config.addDefault("damage-indicator.format.light", "§e");
        config.addDefault("damage-indicator.format.darkness", "§5");
        config.addDefault("damage-indicator.format.divine", "§6");
        
        config.addDefault("damage-indicator.icon.fire", "🔥 ");
        config.addDefault("damage-indicator.icon.ice", "❄ ");
        config.addDefault("damage-indicator.icon.light", "⚡ ");
        config.addDefault("damage-indicator.icon.darkness", "☠ ");
        config.addDefault("damage-indicator.icon.divine", "☀ ");
        
        config.addDefault("combat-tag.duration-seconds", 10);
        
        config.addDefault("stamina.sprint-drain-per-check", 2.0);
        config.addDefault("stamina.check-interval-ticks", 5);
        
        config.addDefault("formulas.defense-divisor", 100.0);
        config.addDefault("formulas.defense-scaling.enabled", true);
        config.addDefault("formulas.defense-scaling.base-divisor", 20.0);
        config.addDefault("formulas.max-mitigation", 0.80);

        // Padrões de Nível
        config.addDefault("level.max-level", 100);
        config.addDefault("level.base-xp", 100.0);
        config.addDefault("level.linear-xp", 15.0);
        config.addDefault("level.exponential-xp", 1.1);
        config.addDefault("level.default-kill-xp", 10.0);
        
        // Attribute Scaling Defaults
        config.addDefault("scaling.strength.physical-damage", 0.5); // 10 STR = +5 DMG
        config.addDefault("scaling.intelligence.mana", 1.0);
        config.addDefault("scaling.intelligence.magic-damage", 1.0);
        config.addDefault("scaling.intelligence.mana-regen", 0.1);
        config.addDefault("scaling.dexterity.crit-chance", 0.2); // 10 DEX = +2% Crit
        config.addDefault("scaling.agility.speed", 0.1); // 10 AGI = +1 Speed
        config.addDefault("scaling.agility.dodge", 0.2); // 10 AGI = +2% Dodge
        config.addDefault("scaling.vitality.health", 5.0); // 10 VIT = +50 HP
        config.addDefault("scaling.vitality.defense", 1.0); // 10 VIT = +10 DEF

        // Formula Defaults
        config.addDefault("formulas.damage-mode", "ADDITIVE"); // ADDITIVE or MULTIPLICATIVE
        config.addDefault("formulas.base-hand-damage", 1.0);
        config.addDefault("formulas.multipliers.strength", 0.01); // 1% per STR
        config.addDefault("formulas.multipliers.intelligence", 0.01); // 1% per INT
        
        // Elemental Relationships (Attacker -> Victim)
        // Fire > Ice (1.5x)
        // Ice > Fire (1.5x) - Mutual weakness or maybe Water > Fire
        // Light > Darkness (1.5x)
        // Darkness > Light (1.5x)
        // Divine > Undead (2.0x) - Special case
        
        config.addDefault("elemental-interactions.enabled", true);

        if (!config.contains("elemental-interactions.fire")) {
            config.set("elemental-interactions.fire.ice", 1.5);
            config.set("elemental-interactions.ice.fire", 1.5); // Melting?
            config.set("elemental-interactions.light.darkness", 1.5);
            config.set("elemental-interactions.darkness.light", 1.5);
            config.set("elemental-interactions.divine.undead", 2.0);
            config.set("elemental-interactions.divine.darkness", 1.5);
            configWrapper.saveConfig();
        }
        
        config.options().copyDefaults(true);
    }

    /**
     * Obtém a XP base configurada para um tipo de entidade específico.
     * Procura em 'level.xp-gain.mobs.<ENTITY_TYPE>'.
     *
     * @param type O tipo de entidade.
     * @return O valor de XP base ou o padrão se não configurado.
     */
    public double getMobExperience(org.bukkit.entity.EntityType type) {
        if (type == null) return xpGainDefaultBase;
        // Permite configurar XP específico por mob, ex: level.xp-gain.mobs.ZOMBIE: 50.0
        return config.getDouble("level.xp-gain.mobs." + type.name(), xpGainDefaultBase);
    }

    private void loadValues() {
        this.indicatorEnabled = config.getBoolean("damage-indicator.enabled");
        this.indicatorDuration = config.getInt("damage-indicator.duration-ticks");
        this.indicatorFormatNormal = config.getString("damage-indicator.format.normal");
        this.indicatorFormatPhysical = config.getString("damage-indicator.format.physical", "§f");
        this.indicatorFormatCritical = config.getString("damage-indicator.format.critical");
        this.indicatorIconCritical = config.getString("damage-indicator.icon.critical");
        this.indicatorIconWeapon = config.getString("damage-indicator.icon.weapon", "⚔ ");
        this.indicatorIconPhysical = config.getString("damage-indicator.icon.physical", "👊 ");
        this.indicatorIconProjectile = config.getString("damage-indicator.icon.projectile", "🏹 ");
        this.indicatorIconMagical = config.getString("damage-indicator.icon.magical", "🔮 ");
        this.indicatorIconEnvironment = config.getString("damage-indicator.icon.environment", "🌲 ");
        this.indicatorIconTrue = config.getString("damage-indicator.icon.true", "🛡 ");
        this.indicatorIconAoE = config.getString("damage-indicator.icon.aoe", "☀ ");
        
        this.indicatorFormatMagical = config.getString("damage-indicator.format.magical", "§d");
        this.indicatorFormatProjectile = config.getString("damage-indicator.format.projectile", "§7");
        this.indicatorFormatEnvironment = config.getString("damage-indicator.format.environment", "§2");
        this.indicatorFormatTrue = config.getString("damage-indicator.format.true", "§f");
        this.indicatorFormatAoE = config.getString("damage-indicator.format.aoe", "§e");

        this.indicatorBackgroundColor = config.getString("damage-indicator.background-color", "#80000000");
        this.indicatorTemplate = config.getString("damage-indicator.template", "%icons% %color%%damage%");
        this.indicatorDecimals = config.getInt("damage-indicator.decimals", 1);
        this.indicatorGravity = config.getDouble("damage-indicator.gravity", 0.04);
        this.indicatorInitialVelocity = config.getDouble("damage-indicator.initial-velocity", 0.25);
        
        elementalFormats.put("fire", config.getString("damage-indicator.format.fire"));
        elementalFormats.put("ice", config.getString("damage-indicator.format.ice"));
        elementalFormats.put("light", config.getString("damage-indicator.format.light"));
        elementalFormats.put("darkness", config.getString("damage-indicator.format.darkness"));
        elementalFormats.put("divine", config.getString("damage-indicator.format.divine"));
        
        elementalIcons.put("fire", config.getString("damage-indicator.icon.fire"));
        elementalIcons.put("ice", config.getString("damage-indicator.icon.ice"));
        elementalIcons.put("light", config.getString("damage-indicator.icon.light"));
        elementalIcons.put("darkness", config.getString("damage-indicator.icon.darkness"));
        elementalIcons.put("divine", config.getString("damage-indicator.icon.divine"));
        
        this.combatTagDuration = config.getLong("combat-tag.duration-seconds") * 1000L;
        
        this.staminaSprintDrain = config.getDouble("stamina.sprint-drain-per-check");
        this.staminaCheckInterval = config.getLong("stamina.check-interval-ticks");
        
        this.defenseDivisor = config.getDouble("formulas.defense-divisor");
        this.defenseScalingEnabled = config.getBoolean("formulas.defense-scaling.enabled", true);
        this.defenseScalingBase = config.getDouble("formulas.defense-scaling.base-divisor", 20.0);
        this.maxMitigation = config.getDouble("formulas.max-mitigation", 0.80);

        this.maxLevel = config.getInt("level.max-level", 100);
        
        // Requirements
        this.xpRequirementsBase = config.getDouble("level.requirements.base-xp", 100.0);
        this.xpRequirementsLinear = config.getDouble("level.requirements.linear-growth", 15.0);
        this.xpRequirementsExponential = config.getDouble("level.requirements.exponential-growth", 1.1);
        
        // XP Gain Core
        this.xpGainDefaultBase = config.getDouble("level.xp-gain.default-base", 10.0);
        
        // XP Gain Variance
        this.xpVarianceEnabled = config.getBoolean("level.xp-gain.variance.enabled", true);
        this.xpVarianceMin = config.getDouble("level.xp-gain.variance.min-factor", 0.8);
        this.xpVarianceMax = config.getDouble("level.xp-gain.variance.max-factor", 1.3);
        
        // XP Gain Scaling
        this.xpScalingMobLevelImpact = config.getBoolean("level.xp-gain.scaling.mob-level-impact", true);
        this.xpScalingMobLevelMultiplier = config.getDouble("level.xp-gain.scaling.mob-level-multiplier", 5.0);
        
        // XP Gain Disparity
        this.xpDisparityEnabled = config.getBoolean("level.xp-gain.disparity.enabled", true);
        
        this.xpPenaltyThreshold = config.getInt("level.xp-gain.disparity.penalty.threshold", 5);
        this.xpPenaltyReduction = config.getDouble("level.xp-gain.disparity.penalty.reduction", 0.15);
        this.xpPenaltyMinCap = config.getDouble("level.xp-gain.disparity.penalty.min-cap", 0.05);
        
        this.xpBonusThreshold = config.getInt("level.xp-gain.disparity.bonus.threshold", 0);
        this.xpBonusIncrement = config.getDouble("level.xp-gain.disparity.bonus.increment", 0.08);
        this.xpBonusMaxCap = config.getDouble("level.xp-gain.disparity.bonus.max-cap", 2.5);
        
        // Attribute Scaling Loading
        this.strengthToPhysicalDamage = config.getDouble("scaling.strength.physical-damage", 0.5);
        this.intelligenceToMana = config.getDouble("scaling.intelligence.mana", 1.0);
        this.intelligenceToMagicDamage = config.getDouble("scaling.intelligence.magic-damage", 1.0);
        this.intelligenceToManaRegen = config.getDouble("scaling.intelligence.mana-regen", 0.1);
        this.dexterityToCritChance = config.getDouble("scaling.dexterity.crit-chance", 0.2);
        this.agilityToSpeed = config.getDouble("scaling.agility.speed", 0.1);
        this.agilityToDodge = config.getDouble("scaling.agility.dodge", 0.2);
        this.vitalityToHealth = config.getDouble("scaling.vitality.health", 5.0);
        this.vitalityToDefense = config.getDouble("scaling.vitality.defense", 1.0);
        
        // Formula Loading
        try {
            this.damageFormulaMode = ScalingMode.valueOf(config.getString("formulas.damage-mode", "ADDITIVE").toUpperCase());
        } catch (IllegalArgumentException e) {
            this.damageFormulaMode = ScalingMode.ADDITIVE;
            plugin.getLogger().warning("Modo de fórmula de dano inválido na config. Usando ADDITIVE.");
        }
        this.baseHandDamage = config.getDouble("formulas.base-hand-damage", 1.0);
        this.strengthMultiplier = config.getDouble("formulas.multipliers.strength", 0.01);
        this.intelligenceMultiplier = config.getDouble("formulas.multipliers.intelligence", 0.01);

        this.elementalInteractionsEnabled = config.getBoolean("elemental-interactions.enabled", true);

        // Load Elemental Multipliers
        elementalMultipliers.clear();
        if (config.isConfigurationSection("elemental-interactions")) {
            for (String attackerElement : config.getConfigurationSection("elemental-interactions").getKeys(false)) {
                if (attackerElement.equals("enabled")) continue; // Skip the enabled flag
                Map<String, Double> victims = new HashMap<>();
                for (String victimElement : config.getConfigurationSection("elemental-interactions." + attackerElement).getKeys(false)) {
                    victims.put(victimElement.toLowerCase(), config.getDouble("elemental-interactions." + attackerElement + "." + victimElement));
                }
                elementalMultipliers.put(attackerElement.toLowerCase(), victims);
            }
        }
    }
}
