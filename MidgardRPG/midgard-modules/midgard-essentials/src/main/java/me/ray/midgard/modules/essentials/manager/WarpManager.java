package me.ray.midgard.modules.essentials.manager;

import me.ray.midgard.modules.essentials.config.EssentialsConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WarpManager {

    private final EssentialsConfig config;
    private final Map<String, Location> warps = new HashMap<>();

    public WarpManager(EssentialsConfig config) {
        this.config = config;
        loadWarps();
    }

    public void loadWarps() {
        warps.clear();
        ConfigurationSection section = config.getConfig().getConfigurationSection("warps");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection warpSection = section.getConfigurationSection(key);
            if (warpSection != null) {
                try {
                    World world = Bukkit.getWorld(warpSection.getString("world"));
                    double x = warpSection.getDouble("x");
                    double y = warpSection.getDouble("y");
                    double z = warpSection.getDouble("z");
                    float yaw = (float) warpSection.getDouble("yaw");
                    float pitch = (float) warpSection.getDouble("pitch");
                    
                    if (world != null) {
                        warps.put(key.toLowerCase(), new Location(world, x, y, z, yaw, pitch));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setWarp(String name, Location location) {
        String key = name.toLowerCase();
        warps.put(key, location);
        
        String path = "warps." + key;
        config.getConfig().set(path + ".world", location.getWorld().getName());
        config.getConfig().set(path + ".x", location.getX());
        config.getConfig().set(path + ".y", location.getY());
        config.getConfig().set(path + ".z", location.getZ());
        config.getConfig().set(path + ".yaw", location.getYaw());
        config.getConfig().set(path + ".pitch", location.getPitch());
        config.save();
    }

    public void deleteWarp(String name) {
        String key = name.toLowerCase();
        warps.remove(key);
        config.getConfig().set("warps." + key, null);
        config.save();
    }

    public Location getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    public Set<String> getWarps() {
        return warps.keySet();
    }
}
