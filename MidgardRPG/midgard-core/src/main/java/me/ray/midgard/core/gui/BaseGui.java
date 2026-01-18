package me.ray.midgard.core.gui;

import me.ray.midgard.core.MidgardCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public abstract class BaseGui implements InventoryHolder {

    protected Inventory inventory;
    protected Player player;

    public BaseGui(Player player, int rows, String title) {
        if (player == null) throw new IllegalArgumentException("Player cannot be null");
        if (rows < 1 || rows > 6) throw new IllegalArgumentException("Rows must be between 1 and 6");

        this.player = player;
        String finalTitle = title != null ? title : "Interface";
        try {
            if (title != null && title.startsWith("key:")) {
                String key = title.substring(4);
                if (MidgardCore.getLanguageManager() != null) {
                    finalTitle = MidgardCore.getLanguageManager().getRawMessage(key);
                } else {
                    finalTitle = key;
                }
            }
        } catch (Exception e) {
            MidgardCore.getInstance().getLogger().warning("Error parsing title key: " + title);
            finalTitle = "Error";
        }
        
        try {
            this.inventory = Bukkit.createInventory(this, rows * 9, me.ray.midgard.core.text.MessageUtils.parse(finalTitle));
        } catch (Exception e) {
             MidgardCore.getInstance().getLogger().severe("Failed to create inventory for " + this.getClass().getSimpleName());
             this.inventory = Bukkit.createInventory(this, 9, net.kyori.adventure.text.Component.text("Error")); // Fallback
        }
    }

    public abstract void initializeItems();

    public void open() {
        initializeItems();
        player.openInventory(inventory);
    }
    
    /**
     * @deprecated Use open() instead.
     */
    @Deprecated
    public void open(Player player) {
        if (this.player != null && !this.player.equals(player)) {
            // Warn or handle mismatch?
        }
        open();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    // Event hooks that implementing classes can override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true); // Default behavior: prevent taking items
    }

    public void onDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    public void onOpen(InventoryOpenEvent event) {}

    public void onClose(InventoryCloseEvent event) {}
}
