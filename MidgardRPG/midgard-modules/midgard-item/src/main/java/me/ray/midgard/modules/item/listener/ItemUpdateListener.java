package me.ray.midgard.modules.item.listener;

import me.ray.midgard.modules.item.ItemModule;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class ItemUpdateListener implements Listener {

    private final ItemModule module;
    @SuppressWarnings("unused")
    private final NamespacedKey idKey;
    @SuppressWarnings("unused")
    private final NamespacedKey revKey;

    public ItemUpdateListener(ItemModule module) {
        this.module = module;
        this.idKey = new NamespacedKey(module.getPlugin(), "midgard_id");
        this.revKey = new NamespacedKey(module.getPlugin(), "midgard_revision");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Schedule update for a few ticks later to ensure player is fully loaded
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    module.getItemManager().updateInventory(player);
                }
            }
        }.runTaskLater(module.getPlugin(), 20L);
    }

    @EventHandler
    public void onInventoryOpen(org.bukkit.event.inventory.InventoryOpenEvent event) {
        // Update items in the opened inventory (chests, etc.)
        // Run 1 tick later to avoid blocking the event and ensure inventory is ready
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                // Check if inventory is still valid/viewed
                if (event.getInventory().getViewers().isEmpty()) return;

                ItemStack[] contents = event.getInventory().getContents();
                boolean updated = false;
                for (int i = 0; i < contents.length; i++) {
                    ItemStack item = contents[i];
                    if (item == null || !item.hasItemMeta()) continue;
                    
                    ItemStack newItem = module.getItemManager().updateItem(item);
                    if (newItem != null) {
                        contents[i] = newItem;
                        updated = true;
                    }
                }
                
                if (updated) {
                    event.getInventory().setContents(contents);
                }
            }
        }.runTaskLater(module.getPlugin(), 1L);
    }
}
