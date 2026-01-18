package me.ray.midgard.modules.item.task;

import me.ray.midgard.modules.item.manager.AttributeUpdater;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EquipmentUpdateTask implements Runnable {

    private static final Map<UUID, Integer> equipmentHashes = new HashMap<>();

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                int currentHash = calculateEquipmentHash(player);
                Integer lastHash = equipmentHashes.get(player.getUniqueId());

                if (lastHash == null || lastHash != currentHash) {
                    AttributeUpdater.updateAttributes(player);
                    equipmentHashes.put(player.getUniqueId(), currentHash);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int calculateEquipmentHash(Player player) {
        ItemStack[] armor = player.getInventory().getArmorContents();
        ItemStack hand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        
        int result = Arrays.hashCode(armor);
        result = 31 * result + (hand != null ? hand.hashCode() : 0);
        result = 31 * result + (offHand != null ? offHand.hashCode() : 0);
        return result;
    }
    
    public static void clearCache(UUID uuid) {
        equipmentHashes.remove(uuid);
    }
}
