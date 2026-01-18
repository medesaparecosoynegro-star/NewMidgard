package me.ray.midgard.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Task {

    private static JavaPlugin plugin;

    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
    }

    public static BukkitTask sync(Runnable runnable) {
        checkInit();
        return Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public static BukkitTask async(Runnable runnable) {
        checkInit();
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    public static BukkitTask syncLater(Runnable runnable, long delayTicks) {
        checkInit();
        return Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks);
    }

    public static BukkitTask asyncLater(Runnable runnable, long delayTicks) {
        checkInit();
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delayTicks);
    }

    public static BukkitTask syncTimer(Runnable runnable, long delayTicks, long periodTicks) {
        checkInit();
        return Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
    }

    public static BukkitTask asyncTimer(Runnable runnable, long delayTicks, long periodTicks) {
        checkInit();
        return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delayTicks, periodTicks);
    }

    private static void checkInit() {
        if (plugin == null) {
            throw new IllegalStateException("Task utility not initialized! Call Task.init(plugin) first.");
        }
    }
}
