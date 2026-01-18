package me.ray.midgard.nms.v1_21;

import me.ray.midgard.nms.api.NMSHandler;
import org.bukkit.entity.Player;

public class NMSHandlerImpl implements NMSHandler {

    @Override
    public void sendPacket(Player player, Object packet) {
        // Implementation for 1.21.10
        // ((org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer) player).getHandle().connection.sendPacket((net.minecraft.network.protocol.Packet<?>) packet);
    }
}
