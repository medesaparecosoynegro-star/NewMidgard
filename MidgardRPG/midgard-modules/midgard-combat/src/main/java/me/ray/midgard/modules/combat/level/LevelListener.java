package me.ray.midgard.modules.combat.level;

import me.ray.midgard.modules.combat.CombatConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Listener para eventos relacionados ao sistema de níveis.
 * <p>
 * Monitora eventos como morte de entidades para conceder experiência aos jogadores.
 */
public class LevelListener implements Listener {

    private final LevelManager levelManager;
    private final CombatConfig config;

    /**
     * Construtor do LevelListener.
     *
     * @param levelManager Gerenciador de níveis.
     * @param config Configuração do módulo de combate.
     */
    public LevelListener(LevelManager levelManager, CombatConfig config) {
        this.levelManager = levelManager;
        this.config = config;
    }

    /**
     * Evento chamado quando uma entidade morre.
     * Verifica se foi morta por um jogador e concede experiência.
     *
     * @param event O evento de morte da entidade.
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        
        // 1. Get Player Data
        me.ray.midgard.core.profile.MidgardProfile profile = me.ray.midgard.core.MidgardCore.getProfileManager().getProfile(killer.getUniqueId());
        if (profile == null) return;
        
        me.ray.midgard.modules.combat.CombatData data = profile.getOrCreateData(me.ray.midgard.modules.combat.CombatData.class);
        int playerLevel = data.getLevel();
        
        // 2. Get Mob Data
        org.bukkit.entity.LivingEntity mob = event.getEntity();
        int mobLevel = getMobLevel(mob);
        double baseMobXp = config.xpGainDefaultBase; // Default fallback
        
        // TODO: Integrate with specific mob config/MythicMobs for base XP
        
        // 3. Calculate Complex XP
        double xp = levelManager.calculateKillXp(playerLevel, mobLevel, baseMobXp);
        
        if (xp <= 0) return;
        
        // 4. Send XP gain message
        if (me.ray.midgard.modules.combat.CombatModule.getInstance() != null) {
            // Using MiniMessage format if available or legacy
            String xpMsg = me.ray.midgard.modules.combat.CombatModule.getInstance()
                .getMessage("progression.xp_gained");
                
            if (xpMsg != null) {
                xpMsg = xpMsg.replace("%xp%", String.format("%.1f", xp))
                             .replace("%mob%", mob.getName())
                             .replace("%level%", String.valueOf(mobLevel)); // Extra placeholder
                me.ray.midgard.core.text.MessageUtils.send(killer, xpMsg);
            }
        }
        
        levelManager.addExperience(killer, xp);
    }
    
    /**
     * Tenta extrair o nível do mob de várias fontes.
     */
    private int getMobLevel(org.bukkit.entity.LivingEntity mob) {
        // 1. Try Metadata (set by Midgard or other plugins)
        if (mob.hasMetadata("midgard_level")) {
            return mob.getMetadata("midgard_level").get(0).asInt();
        }
        
        // 2. Try Name Pattern "[Lv. 10] Zombie" or "Zombie [10]"
        String name = org.bukkit.ChatColor.stripColor(mob.getCustomName() != null ? mob.getCustomName() : mob.getName());
        if (name != null) {
            // Regex simples para capturar números perto de "Lv" ou "Lvl" ou entre colchetes
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\[(?:Lv\\.?|Lvl\\.?)?\\s*(\\d+)\\]");
            java.util.regex.Matcher m = p.matcher(name);
            if (m.find()) {
                try {
                    return Integer.parseInt(m.group(1));
                } catch (NumberFormatException ignored) {}
            }
        }
        
        return 1; // Default level
    }
}
