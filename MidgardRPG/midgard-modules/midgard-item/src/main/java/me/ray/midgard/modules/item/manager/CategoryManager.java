package me.ray.midgard.modules.item.manager;

import me.ray.midgard.modules.item.ItemModule;
import me.ray.midgard.modules.item.model.ItemCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class CategoryManager {

    private final ItemModule module;
    private final Map<String, ItemCategory> categories;

    public CategoryManager(ItemModule module) {
        this.module = module;
        this.categories = new HashMap<>();
    }

    public void loadCategories() {
        categories.clear();
        File file = new File(module.getDataFolder(), "item-types.yml");
        if (!file.exists()) {
            // Save from modules/item/item-types.yml in jar to modules/item/item-types.yml in disk
            module.saveResource("modules/item/item-types.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            try {
                ConfigurationSection section = config.getConfigurationSection(key);
                if (section != null) {
                    ItemCategory category = new ItemCategory(key, section);
                    categories.put(key, category);
                }
            } catch (Exception e) {
                module.getPlugin().getLogger().log(Level.WARNING, "Erro ao carregar categoria " + key, e);
            }
        }
        module.getPlugin().getLogger().info("Carregadas " + categories.size() + " categorias.");
    }

    public ItemCategory getCategory(String id) {
        return categories.get(id);
    }

    public Collection<ItemCategory> getCategories() {
        return categories.values();
    }
}
