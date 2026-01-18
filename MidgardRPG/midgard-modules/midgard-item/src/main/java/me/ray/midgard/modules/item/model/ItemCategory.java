package me.ray.midgard.modules.item.model;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class ItemCategory {

    private final String id;
    private final String name;
    private final Material icon;
    private final int modelData;
    private final int slot;
    private final int page;

    public ItemCategory(String id, String name, Material icon, int modelData, int slot, int page) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.modelData = modelData;
        this.slot = slot;
        this.page = page;
    }

    public ItemCategory(String id, ConfigurationSection section) {
        this.id = id;
        this.name = section.getString("name", id);
        this.icon = Material.valueOf(section.getString("icon", "CHEST").toUpperCase());
        this.modelData = section.getInt("model-data", 0);
        this.slot = section.getInt("slot", -1);
        this.page = section.getInt("page", 1);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Material getIcon() {
        return icon;
    }

    public int getModelData() {
        return modelData;
    }

    public int getSlot() {
        return slot;
    }

    public int getPage() {
        return page;
    }
    
    public ItemStack getIconItem() {
        // Helper to get the display item for the GUI
        return new ItemStack(icon); // Simplified, will be enhanced in GUI
    }
}
