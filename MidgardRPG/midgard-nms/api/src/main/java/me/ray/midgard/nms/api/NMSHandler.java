package me.ray.midgard.nms.api;

import org.bukkit.entity.Player;

public interface NMSHandler {
    
    void sendPacket(Player player, Object packet);
    
    // Add other NMS methods here
}
