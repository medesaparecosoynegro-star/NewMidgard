package me.ray.midgard.modules.combat;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.attribute.AttributeInstance;
import me.ray.midgard.core.attribute.CoreAttributeData;
import me.ray.midgard.core.profile.MidgardProfile;

import me.ray.midgard.modules.combat.listener.StatScalingListener;
import me.ray.midgard.modules.combat.task.RegenerationTask;
import me.ray.midgard.modules.combat.task.StaminaTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import java.util.logging.Level;

/**
 * Gerenciador principal de combate.
 * <p>
 * Esta classe é responsável por orquestrar as mecânicas de combate em tempo real, incluindo:
 * <ul>
 *     <li>Regeneração de Vida, Mana e Stamina.</li>
 *     <li>Consumo de Stamina ao correr.</li>
 *     <li>Gerenciamento de "Combat Tag" (estado de combate).</li>
 *     <li>Processamento de eventos de dano e aplicação de fórmulas de combate.</li>
 * </ul>
 */
public class CombatManager {
    
    private static CombatManager instance;
    private final JavaPlugin plugin;
    private final CombatConfig config;
    @SuppressWarnings("unused")
    private final DamageIndicatorManager indicatorManager;
    private final DamageHandler damageHandler;
    private final Map<UUID, Long> combatTag = new HashMap<>();
    private final java.util.Set<UUID> debugPlayers = new java.util.HashSet<>();
    private final me.ray.midgard.modules.combat.debug.CombatDebugScoreboard debugScoreboard;
    
    // Tasks
    private final RegenerationTask regenerationTask;
    private final StaminaTask staminaTask;

    /**
     * Construtor do CombatManager.
     *
     * @param plugin Instância do plugin principal.
     * @param config Configuração do módulo de combate.
     * @param indicatorManager Gerenciador de indicadores de dano para exibição visual.
     */
    public CombatManager(JavaPlugin plugin, CombatConfig config, DamageIndicatorManager indicatorManager) {
        instance = this;
        this.plugin = plugin;
        this.config = config;
        this.indicatorManager = indicatorManager;
        this.debugScoreboard = new me.ray.midgard.modules.combat.debug.CombatDebugScoreboard();
        this.damageHandler = new DamageHandler(this, config, indicatorManager);
        
        // Initialize Tasks
        this.regenerationTask = new RegenerationTask(this, combatTag);
        this.staminaTask = new StaminaTask(plugin, config);
        
        // Register Listeners
        plugin.getServer().getPluginManager().registerEvents(new CombatListener(damageHandler), plugin);
        plugin.getServer().getPluginManager().registerEvents(new StatScalingListener(), plugin);
    }

    public static CombatManager getInstance() {
        return instance;
    }

    public DamageHandler getDamageHandler() {
        return damageHandler;
    }

    public CombatConfig getConfig() {
        return config;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }
    
    public void updateCombatTag(UUID uuid) {
        combatTag.put(uuid, System.currentTimeMillis() + config.combatTagDuration);
    }
    
    public Map<UUID, Long> getCombatTag() {
        return combatTag;
    }

    /**
     * Inicia as tarefas agendadas do gerenciador.
     * Inicia o loop de regeneração e a verificação de stamina.
     */
    public void start() {
        // Run Regeneration every second (20 ticks)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                regenerationTask.run();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Erro na tarefa de combate (Regen)", e);
            }
        }, 20L, 20L);
        
        // Run Stamina Task
        Bukkit.getScheduler().runTaskTimer(plugin, staminaTask, config.staminaCheckInterval, config.staminaCheckInterval);
    }

    /**
     * Sincroniza a vida visual do jogador com a vida do RPG.
     *
     * @param player Jogador.
     * @param currentHealth Vida atual do RPG.
     * @param maxHealth Vida máxima do RPG.
     */
    public void syncHealth(Player player, double currentHealth, double maxHealth) {
        // Escala a vida vanilla (0-20) para representar a porcentagem de vida do RPG
        double percent = currentHealth / maxHealth;
        double vanillaHealth = percent * 20.0;
        
        // Limitar (Clamp)
        vanillaHealth = Math.max(0, Math.min(20, vanillaHealth));
        
        // Garante que a vida máxima visual seja 20
        if (player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue() != 20.0) {
            player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(20.0);
        }
        
        player.setHealth(vanillaHealth);
    }

    /**
     * Calcula o tempo de recarga ajustado após aplicar a Redução de Cooldown (CDR).
     *
     * @param player O jogador para verificar os atributos.
     * @param baseCooldownMillis O tempo de recarga base em milissegundos.
     * @return O tempo de recarga ajustado em milissegundos.
     */
    public static long getAdjustedCooldown(Player player, long baseCooldownMillis) {
        MidgardProfile profile = MidgardCore.getProfileManager().getProfile(player.getUniqueId());
        if (profile == null) return baseCooldownMillis;
        
        CoreAttributeData attributeData = profile.getOrCreateData(CoreAttributeData.class);
        AttributeInstance cdrAttr = attributeData.getInstance(CombatAttributes.COOLDOWN_REDUCTION);
        
        if (cdrAttr != null && cdrAttr.getValue() > 0) {
            double cdr = Math.min(80.0, cdrAttr.getValue()); // Limite de 80% geralmente
            return (long) (baseCooldownMillis * (1.0 - (cdr / 100.0)));
        }
        
        return baseCooldownMillis;
    }

    public boolean isDebugging(UUID uuid) {
        return debugPlayers.contains(uuid);
    }

    public void toggleDebug(UUID uuid) {
        if (debugPlayers.contains(uuid)) {
            debugPlayers.remove(uuid);
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) debugScoreboard.disable(p);
        } else {
            debugPlayers.add(uuid);
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) debugScoreboard.enable(p);
        }
    }
    
    public me.ray.midgard.modules.combat.debug.CombatDebugScoreboard getDebugScoreboard() {
        return debugScoreboard;
    }
}
