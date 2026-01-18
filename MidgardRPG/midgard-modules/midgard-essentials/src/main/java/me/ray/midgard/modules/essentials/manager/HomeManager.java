package me.ray.midgard.modules.essentials.manager;

import me.ray.midgard.core.config.ConfigWrapper;
import me.ray.midgard.modules.essentials.config.EssentialsConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HomeManager {

    private final ConfigWrapper homesConfig;
    private final EssentialsConfig config;
    // Cache: UUID -> (HomeName -> Location)
    private final Map<UUID, Map<String, Location>> homesCache = new HashMap<>();

    public HomeManager(JavaPlugin plugin, EssentialsConfig config) {
        this.homesConfig = new ConfigWrapper(plugin, "data/homes.yml");
        this.config = config;
        loadHomes();
    }

    public void loadHomes() {
        homesCache.clear();
        ConfigurationSection section = homesConfig.getConfig().getConfigurationSection("homes");
        if (section == null) return;

        for (String uuidStr : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection playerSection = section.getConfigurationSection(uuidStr);
                if (playerSection == null) continue;

                Map<String, Location> playerHomes = new HashMap<>();
                for (String homeName : playerSection.getKeys(false)) {
                    ConfigurationSection homeLoc = playerSection.getConfigurationSection(homeName);
                    if (homeLoc != null) {
                        World world = Bukkit.getWorld(homeLoc.getString("world"));
                        double x = homeLoc.getDouble("x");
                        double y = homeLoc.getDouble("y");
                        double z = homeLoc.getDouble("z");
                        float yaw = (float) homeLoc.getDouble("yaw");
                        float pitch = (float) homeLoc.getDouble("pitch");

                        if (world != null) {
                            playerHomes.put(homeName.toLowerCase(), new Location(world, x, y, z, yaw, pitch));
                        }
                    }
                }
                homesCache.put(uuid, playerHomes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setHome(Player player, String homeName, Location location) {
        UUID uuid = player.getUniqueId();
        homesCache.computeIfAbsent(uuid, k -> new HashMap<>()).put(homeName.toLowerCase(), location);

        String path = "homes." + uuid.toString() + "." + homeName.toLowerCase();
        homesConfig.getConfig().set(path + ".world", location.getWorld().getName());
        homesConfig.getConfig().set(path + ".x", location.getX());
        homesConfig.getConfig().set(path + ".y", location.getY());
        homesConfig.getConfig().set(path + ".z", location.getZ());
        homesConfig.getConfig().set(path + ".yaw", location.getYaw());
        homesConfig.getConfig().set(path + ".pitch", location.getPitch());
        homesConfig.saveConfig();
    }

    public void deleteHome(Player player, String homeName) {
        UUID uuid = player.getUniqueId();
        if (homesCache.containsKey(uuid)) {
            homesCache.get(uuid).remove(homeName.toLowerCase());
        }
        homesConfig.getConfig().set("homes." + uuid.toString() + "." + homeName.toLowerCase(), null);
        homesConfig.saveConfig();
    }

    public Location getHome(Player player, String homeName) {
        Map<String, Location> playerHomes = homesCache.get(player.getUniqueId());
        if (playerHomes != null) {
            return playerHomes.get(homeName.toLowerCase());
        }
        return null;
    }

    public Set<String> getHomes(Player player) {
        Map<String, Location> playerHomes = homesCache.get(player.getUniqueId());
        if (playerHomes != null) {
            return playerHomes.keySet();
        }
        return Set.of();
    }
    
    public int getHomeLimit(Player player) {
        // TODO: Permission based limits
        return config.getConfig().getInt("homes.limit", 3);
    }
    
    public int getHomeCount(Player player) {
        Map<String, Location> playerHomes = homesCache.get(player.getUniqueId());
        return playerHomes == null ? 0 : playerHomes.size();
    }
}
