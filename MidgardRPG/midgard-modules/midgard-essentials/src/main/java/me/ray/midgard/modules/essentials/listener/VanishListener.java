package me.ray.midgard.modules.essentials.listener;

import me.ray.midgard.modules.essentials.manager.EssentialsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class VanishListener implements Listener {

    private final EssentialsManager manager;

    public VanishListener(EssentialsManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try {
            if (manager != null && manager.getVanishManager() != null) {
                manager.getVanishManager().updateFor(event.getPlayer());
            }
        } catch (Exception e) {
             // Log via plugin if we can access it, or just print stack trace safer?
             // essentials doesn't expose plugin easily here, but manager might have config with plugin access?
             // EssentialsManager has EssentialsConfig which has JavaPlugin.
             // But simpler to just use Bukkit.getLogger orSystem.err if we assume severe.
             // Or construct with plugin.
             org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao atualizar vanish para jogador " + event.getPlayer().getName(), e);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        try {
            if (manager != null && manager.getVanishManager() != null) {
                if (manager.getVanishManager().isVanished(event.getPlayer())) {
                    manager.getVanishManager().removePlayer(event.getPlayer());
                    event.quitMessage(null);
                }
            }
        } catch (Exception e) {
             org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao processar quit vanish para jogador " + event.getPlayer().getName(), e);
        }
    }
}
