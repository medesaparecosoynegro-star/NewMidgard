package me.ray.midgard.modules.item.utils;

import me.ray.midgard.modules.item.ItemModule;
import me.ray.midgard.modules.item.model.ItemStat;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class ItemPDC {

    private static final NamespacedKey ID_KEY = new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_id");

    public static void setString(ItemMeta meta, String key, String value) {
        meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), key), PersistentDataType.STRING, value);
    }

    public static String getString(ItemMeta meta, String key) {
        return meta.getPersistentDataContainer().get(new NamespacedKey(ItemModule.getInstance().getPlugin(), key), PersistentDataType.STRING);
    }

    public static void setDouble(ItemMeta meta, String key, double value) {
        meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), key), PersistentDataType.DOUBLE, value);
    }

    public static double getDouble(ItemMeta meta, String key) {
        return meta.getPersistentDataContainer().getOrDefault(new NamespacedKey(ItemModule.getInstance().getPlugin(), key), PersistentDataType.DOUBLE, 0.0);
    }
    
    public static boolean has(ItemMeta meta, String key) {
        return meta.getPersistentDataContainer().has(new NamespacedKey(ItemModule.getInstance().getPlugin(), key), PersistentDataType.STRING) ||
               meta.getPersistentDataContainer().has(new NamespacedKey(ItemModule.getInstance().getPlugin(), key), PersistentDataType.DOUBLE) ||
               meta.getPersistentDataContainer().has(new NamespacedKey(ItemModule.getInstance().getPlugin(), key), PersistentDataType.INTEGER);
    }

    public static void setStat(ItemMeta meta, ItemStat stat, double value) {
        setDouble(meta, "stat_" + stat.name().toLowerCase(), value);
    }

    public static double getStat(ItemMeta meta, ItemStat stat) {
        return getDouble(meta, "stat_" + stat.name().toLowerCase());
    }
    
    public static boolean hasStat(ItemMeta meta, ItemStat stat) {
        return meta.getPersistentDataContainer().has(new NamespacedKey(ItemModule.getInstance().getPlugin(), "stat_" + stat.name().toLowerCase()), PersistentDataType.DOUBLE);
    }

    public static Map<ItemStat, Double> getStats(ItemStack item) {
        Map<ItemStat, Double> stats = new HashMap<>();
        if (item == null || !item.hasItemMeta()) return stats;
        
        ItemMeta meta = item.getItemMeta();
        for (ItemStat stat : ItemStat.values()) {
            if (hasStat(meta, stat)) {
                stats.put(stat, getStat(meta, stat));
            }
        }
        return stats;
    }
    
    public static void setMidgardId(ItemMeta meta, String id) {
        meta.getPersistentDataContainer().set(ID_KEY, PersistentDataType.STRING, id);
    }
    
    public static String getMidgardId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(ID_KEY, PersistentDataType.STRING);
    }
}
