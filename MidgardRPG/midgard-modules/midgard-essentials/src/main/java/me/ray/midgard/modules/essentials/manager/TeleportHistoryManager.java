package me.ray.midgard.modules.essentials.manager;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportHistoryManager {

    private final Map<UUID, Location> lastLocations = new HashMap<>();

    public void setLastLocation(Player player, Location location) {
        lastLocations.put(player.getUniqueId(), location);
    }

    public Location getLastLocation(Player player) {
        return lastLocations.get(player.getUniqueId());
    }

    public boolean hasLastLocation(Player player) {
        return lastLocations.containsKey(player.getUniqueId());
    }
    
    public void clearHistory(Player player) {
        lastLocations.remove(player.getUniqueId());
    }
}
