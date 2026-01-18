package me.ray.midgard.modules.item;

import me.ray.midgard.core.ModulePriority;
import me.ray.midgard.core.RPGModule;
import me.ray.midgard.modules.item.command.ItemCommand;
import me.ray.midgard.modules.item.manager.CategoryManager;
import me.ray.midgard.modules.item.manager.ItemManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ItemModule extends RPGModule {

    private static ItemModule instance;
    private ItemManager itemManager;
    private CategoryManager categoryManager;
    private me.ray.midgard.modules.item.gui.ItemEditionMessagesLoader itemEditionLoader;
    private me.ray.midgard.modules.item.task.EquipmentUpdateTask equipmentUpdateTask;

    public ItemModule() {
        super("MidgardItem", ModulePriority.NORMAL);
    }

    @Override
    public void onEnable() {
        instance = this;
        plugin.getLogger().info("Habilitando Midgard-Item...");

        // Ensure module folder exists
        // Super reloadConfig handles config.yml creation via ConfigWrapper

        // Save other default files
        saveExtraResources();

        // Inicializar gerenciadores
        this.categoryManager = new CategoryManager(this);
        this.categoryManager.loadCategories();

        this.itemEditionLoader = new me.ray.midgard.modules.item.gui.ItemEditionMessagesLoader(this);
        this.itemEditionLoader.load();

        this.itemManager = new ItemManager(this);
        this.itemManager.loadItems();
        
        // Registrar comando item apenas no AdminCommand para /rpg admin item
        if (me.ray.midgard.core.MidgardCore.getAdminCommand() != null) {
            me.ray.midgard.core.MidgardCore.getAdminCommand().registerSubcommand(new ItemCommand(this));
        }

        // Registrar listeners
        plugin.getServer().getPluginManager().registerEvents(new me.ray.midgard.modules.item.listener.ChatInputListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new me.ray.midgard.modules.item.listener.ItemUpdateListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new me.ray.midgard.modules.item.listener.GemListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new me.ray.midgard.modules.item.listener.EquipListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new me.ray.midgard.modules.item.listener.ItemRestrictionListener(this), plugin);

        // Iniciar tarefas
        this.equipmentUpdateTask = new me.ray.midgard.modules.item.task.EquipmentUpdateTask();
        plugin.getServer().getScheduler().runTaskTimer(plugin, this.equipmentUpdateTask, 20L, 10L); // Run every 10 ticks (0.5s)

        plugin.getLogger().info("Midgard-Item habilitado com sucesso!");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (itemEditionLoader != null) itemEditionLoader.load();
        if (categoryManager != null) categoryManager.loadCategories();
        if (itemManager != null) itemManager.loadItems();
    }

    @Override
    public void onDisable() {
        plugin.getLogger().info("Desabilitando Midgard-Item...");
        instance = null;
    }

    public static ItemModule getInstance() {
        return instance;
    }

    public me.ray.midgard.modules.item.gui.ItemEditionMessagesLoader getItemEditionLoader() {
        return itemEditionLoader;
    }

    public File getDataFolder() {
        return new File(plugin.getDataFolder(), "modules/item");
    }

    public void saveExtraResources() {
        File folder = getDataFolder();
        if (!folder.exists()) folder.mkdirs();
        
        String[] resources = {"item-tiers.yml", "item-sets.yml", "custom-stats.yml", "item-types.yml"};
        for (String res : resources) {
            File resFile = new File(folder, res);
            if (!resFile.exists()) {
                // Try to find in standard path or module path
                try {
                    plugin.saveResource("modules/item/" + res, false);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Could not save resource " + res + ": " + e.getMessage());
                }
            }
        }

        // Save Item Edition GUI files
        String editionPath = "messages/item/gui/item_edition/";
        String[] editionFiles = {
            "_main.yml",
            "editors.yml",
            "categories/attributes.yml",
            "categories/combat_defense.yml",
            "categories/combat_offense.yml",
            "categories/consumables.yml",
            "categories/display.yml",
            "categories/general.yml",
            "categories/misc.yml",
            "categories/restrictions.yml",
            "categories/tools.yml"
        };
        
        for (String file : editionFiles) {
             // Check: plugins/MidgardLoader/messages/item/gui/item_edition/file
             File f = new File(plugin.getDataFolder(), "messages/item/gui/item_edition/" + file);
             if (!f.exists()) {
                 try {
                     plugin.saveResource(editionPath + file, false);
                 } catch (Exception e) {
                     plugin.getLogger().warning("Could not save resource " + file + ": " + e.getMessage());
                 }
             }
        }

        // Save item category files
        String[] itemTypes = {
            "accessory", "armor", "axe", "block", "bow", "catalyst", "club", "consumable", 
            "crossbow", "dagger", "gauntlet", "gem_stone", "greataxe", 
            "greatbow", "greathammer", "greatstaff", "greatsword", "halberd", "hammer", 
            "katana", "lance", "long_sword", "lute", "main_catalyst", "material", 
            "miscellaneous", "musket", "off_catalyst", "ornament", "shield", "skin", 
            "spear", "staff", "sword", "talisman", "thrusting_sword", "tome", "tool", 
            "wand", "whip"
        };

        for (String type : itemTypes) {
            File categoryFolder = new File(getDataFolder(), type);
            if (!categoryFolder.exists()) {
                categoryFolder.mkdirs();
            }

            String exampleName = "example_" + type + ".yml";
            File itemFile = new File(categoryFolder, exampleName);
            
            if (!itemFile.exists()) {
                try {
                    plugin.saveResource("modules/item/" + type + "/" + exampleName, false);
                } catch (Exception e) {
                    // Mute warning for example items not found, they might not all exist
                }
            }
        }
    }
    
    public void saveResource(String resourcePath, boolean replace) {
        plugin.saveResource(resourcePath, replace);
    }

    public ItemManager getItemManager() {
        return itemManager;
    }
    
    public CategoryManager getCategoryManager() {
        return categoryManager;
    }
}
