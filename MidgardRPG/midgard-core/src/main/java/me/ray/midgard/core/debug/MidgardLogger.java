package me.ray.midgard.core.debug;

import org.bukkit.Bukkit;

public class MidgardLogger {

    private static final String PREFIX = "§8[§bMidgard§8] ";
    private static boolean debugEnabled = false;
    
    // ============================================
    // INFO METHODS
    // ============================================
    
    public static void info(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + "§f" + message);
    }
    
    public static void info(String format, Object... args) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + "§f" + String.format(format, args));
    }
    
    // ============================================
    // WARN METHODS
    // ============================================

    public static void warn(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + "§eWARN: " + message);
    }
    
    public static void warn(String format, Object... args) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + "§eWARN: " + String.format(format, args));
    }
    
    // ============================================
    // ERROR METHODS
    // ============================================

    public static void error(String message) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + "§cERRO: " + message);
    }
    
    public static void error(String format, Object... args) {
        // Verificar se o último argumento é uma Throwable
        Throwable throwable = null;
        Object[] formatArgs = args;
        
        if (args.length > 0 && args[args.length - 1] instanceof Throwable) {
            throwable = (Throwable) args[args.length - 1];
            formatArgs = new Object[args.length - 1];
            System.arraycopy(args, 0, formatArgs, 0, formatArgs.length);
        }
        
        Bukkit.getConsoleSender().sendMessage(PREFIX + "§cERRO: " + String.format(format, formatArgs));
        
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }
    
    public static void error(String message, Throwable t) {
        Bukkit.getConsoleSender().sendMessage(PREFIX + "§cERRO: " + message);
        t.printStackTrace();
    }
    
    // ============================================
    // DEBUG METHODS
    // ============================================
    
    public static void debug(String message) {
        if (debugEnabled) {
            Bukkit.getConsoleSender().sendMessage(PREFIX + "§7DEBUG: " + message);
        }
    }
    
    public static void debug(String format, Object... args) {
        if (debugEnabled) {
            Bukkit.getConsoleSender().sendMessage(PREFIX + "§7DEBUG: " + String.format(format, args));
        }
    }

    // Métodos com categoria para manter compatibilidade
    public static void debug(DebugCategory category, String module, String title, String... details) {
        if (debugEnabled) {
            Bukkit.getConsoleSender().sendMessage(PREFIX + "§7DEBUG [" + category + "]: " + module + " - " + title);
        }
    }

    public static void debug(DebugCategory category, String message, Object... args) {
        if (debugEnabled) {
            Bukkit.getConsoleSender().sendMessage(PREFIX + "§7DEBUG [" + category + "]: " + String.format(message, args));
        }
    }

    // ============================================
    // CONFIGURATION
    // ============================================
    
    public static void setDebugEnabled(boolean v) {
        debugEnabled = v;
    }
    
    public static boolean isDebugEnabled() { 
        return debugEnabled; 
    }
    
    public static void setActiveCategory(DebugCategory c) {}
    public static DebugCategory getActiveCategory() { return null; }
    public static boolean isCategoryEnabled(DebugCategory c) { return debugEnabled; }
    public static void updateSettings(boolean b, java.util.Set<DebugCategory> s) {
        debugEnabled = b;
    }
}
