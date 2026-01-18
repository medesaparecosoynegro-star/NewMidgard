package me.ray.midgard.modules.essentials.manager;

import me.ray.midgard.modules.essentials.config.EssentialsConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class SpawnManager {

    private final EssentialsConfig config;
    private Location spawnLocation;

    public SpawnManager(EssentialsConfig config) {
        this.config = config;
        loadSpawn();
    }

    public void loadSpawn() {
        ConfigurationSection section = config.getConfig().getConfigurationSection("spawn.location");
        if (section != null) {
            try {
                World world = Bukkit.getWorld(section.getString("world"));
                double x = section.getDouble("x");
                double y = section.getDouble("y");
                double z = section.getDouble("z");
                float yaw = (float) section.getDouble("yaw");
                float pitch = (float) section.getDouble("pitch");

                if (world != null) {
                    spawnLocation = new Location(world, x, y, z, yaw, pitch);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setSpawn(Location location) {
        this.spawnLocation = location;
        String path = "spawn.location";
        config.getConfig().set(path + ".world", location.getWorld().getName());
        config.getConfig().set(path + ".x", location.getX());
        config.getConfig().set(path + ".y", location.getY());
        config.getConfig().set(path + ".z", location.getZ());
        config.getConfig().set(path + ".yaw", location.getYaw());
        config.getConfig().set(path + ".pitch", location.getPitch());
        config.save();
    }

    public Location getSpawn() {
        return spawnLocation;
    }
}
