package me.ray.midgard.core;

import me.ray.midgard.core.config.ConfigWrapper;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Abstract base class for all MidgardRPG modules.
 */
public abstract class RPGModule {

    private final String name;
    private boolean enabled = false;
    private ModulePriority priority = ModulePriority.NORMAL;
    
    protected JavaPlugin plugin;
    protected ConfigWrapper config;

    public RPGModule(String name) {
        this.name = name;
    }

    public RPGModule(String name, ModulePriority priority) {
        this.name = name;
        this.priority = priority;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Called when the module is enabled.
     * @param plugin The main plugin instance.
     */
    public void onEnable(JavaPlugin plugin) {
        this.plugin = plugin;
        reloadConfig();
        onEnable();
    }
    
    /**
     * Abstract method to be implemented by modules.
     */
    public abstract void onEnable();

    /**
     * Called when the module is disabled.
     * @param plugin The main plugin instance.
     */
    public void onDisable(JavaPlugin plugin) {
        onDisable();
        this.plugin = null;
        this.config = null;
    }

    /**
     * Abstract method to be implemented by modules.
     */
    public abstract void onDisable();
    
    public void reloadConfig() {
        if (plugin == null) return;
        // Default path: modules/<module_name>/config.yml
        // Normalized name to lowercase for folder structure (optional but good practice)
        String path = "modules/" + name.toLowerCase().replace("midgard", "") + "/config.yml";
        // Correction: if name is "MidgardEssentials", path becomes "modules/essentials/config.yml"
        
        // Handle edge case if name doesn't start with Midgard
        if (path.startsWith("modules//")) {
             path = "modules/" + name.toLowerCase() + "/config.yml";
        }
        
        this.config = new ConfigWrapper(plugin, path);
    }
    
    public FileConfiguration getConfig() {
        if (config == null) reloadConfig();
        return config.getConfig();
    }
    
    public void saveConfig() {
        if (config != null) config.saveConfig();
    }

    public String getName() {
        return name;
    }

    public ModulePriority getPriority() {
        return priority;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
