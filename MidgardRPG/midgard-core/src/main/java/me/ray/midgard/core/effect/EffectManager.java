package me.ray.midgard.core.effect;

import me.ray.midgard.core.profile.MidgardProfile;
import me.ray.midgard.core.profile.ProfileManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EffectManager {

    private final JavaPlugin plugin;
    private final ProfileManager profileManager;

    public EffectManager(JavaPlugin plugin, ProfileManager profileManager) {
        this.plugin = plugin;
        this.profileManager = profileManager;
        startTask();
    }

    private void startTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                MidgardProfile profile = profileManager.getProfile(player.getUniqueId());
                if (profile == null) continue;
                
                EffectData data = profile.getData(EffectData.class);
                if (data == null || data.getActiveEffects().isEmpty()) continue;
                
                for (ActiveEffect effect : data.getActiveEffects()) {
                    boolean expired = effect.tick(profile);
                    if (expired) {
                        effect.end(profile);
                        data.removeEffect(effect);
                    }
                }
            }
        }, 1L, 1L);
    }
    
    public void applyEffect(Player player, StatusEffect effect, long duration) {
        MidgardProfile profile = profileManager.getProfile(player.getUniqueId());
        if (profile == null) return;
        
        EffectData data = profile.getOrCreateData(EffectData.class);
        ActiveEffect active = new ActiveEffect(effect, duration, player.getUniqueId());
        data.addEffect(active);
        active.start(profile);
    }
}
