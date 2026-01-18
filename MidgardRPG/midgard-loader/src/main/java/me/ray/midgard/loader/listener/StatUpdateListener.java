package me.ray.midgard.loader.listener;

import me.ray.midgard.modules.item.manager.AttributeUpdater;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class StatUpdateListener implements Listener {

    private final JavaPlugin plugin;

    public StatUpdateListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        updateStats(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            Bukkit.getScheduler().runTask(plugin, () -> updateStats(player));
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> updateStats(event.getPlayer()));
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> updateStats(event.getPlayer()));
    }
    
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> updateStats(event.getPlayer()));
    }

    private void updateStats(Player player) {
        // Delegate to AttributeUpdater to avoid conflicts and ensure consistency
        AttributeUpdater.updateAttributes(player);
    }
}
