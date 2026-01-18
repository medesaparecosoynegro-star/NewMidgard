package me.ray.midgard.modules.essentials.utils;

import me.ray.midgard.core.command.MidgardCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

public class CommandRegistry {

    private static CommandMap commandMap;

    static {
        try {
            Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            commandMap = (CommandMap) f.get(Bukkit.getServer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void register(JavaPlugin plugin, MidgardCommand command, String... aliases) {
        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);
            PluginCommand cmd = c.newInstance(command.getName(), plugin);
            
            cmd.setExecutor(command);
            cmd.setTabCompleter(command);
            cmd.setAliases(Arrays.asList(aliases));
            // cmd.setDescription(command.getDescription()); // If MidgardCommand had description
            
            commandMap.register(plugin.getName(), cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
