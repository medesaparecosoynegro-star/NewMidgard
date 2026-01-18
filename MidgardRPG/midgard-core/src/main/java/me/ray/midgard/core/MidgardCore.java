package me.ray.midgard.core;

import me.ray.midgard.core.command.AdminCommandRegistry;
import me.ray.midgard.core.command.CommandManager;
import me.ray.midgard.core.debug.MidgardProfiler;
import me.ray.midgard.core.economy.EconomyProvider;
import me.ray.midgard.core.gui.InventoryProtectionManager;
import me.ray.midgard.core.i18n.LanguageManager;
import me.ray.midgard.core.integration.VaultIntegration;
import me.ray.midgard.core.integration.WorldGuardIntegration;
import me.ray.midgard.core.placeholder.PlaceholderRegistry;
import me.ray.midgard.core.profile.ProfileManager;
import me.ray.midgard.core.region.RegionManager;
import me.ray.midgard.nms.api.NMSHandler;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Classe principal do núcleo do MidgardRPG.
 * Gerencia a inicialização e acesso aos principais gerenciadores.
 */
public class MidgardCore {
    
    private static JavaPlugin pluginInstance;
    private static EconomyProvider economyProvider;
    private static ProfileManager profileManager;
    private static LanguageManager languageManager;
    private static CommandManager commandManager;
    private static AdminCommandRegistry adminCommandRegistry;
    private static me.ray.midgard.core.loot.LootManager lootManager;
    private static InventoryProtectionManager inventoryProtectionManager;
    private static PlaceholderRegistry placeholderRegistry;
    private static NMSHandler nmsHandler;
    private static ModuleManager moduleManager;
    
    private static boolean loaded = false;

    /**
     * Inicializa o núcleo do plugin.
     *
     * @param plugin Instância do plugin principal.
     * @param pm Gerenciador de perfis.
     * @param lm Gerenciador de idiomas.
     */
    public static void init(JavaPlugin plugin, ProfileManager pm, LanguageManager lm) {
        if (loaded) {
            plugin.getLogger().warning("Tentativa de inicializar MidgardCore duas vezes!");
            return;
        }
        
        try {
            pluginInstance = plugin;
            profileManager = pm;
            languageManager = lm;
            commandManager = new CommandManager();
            lootManager = new me.ray.midgard.core.loot.LootManager(plugin);
            inventoryProtectionManager = new InventoryProtectionManager(plugin);
            plugin.getServer().getPluginManager().registerEvents(inventoryProtectionManager, plugin);
            
            // Initialize Profiler
            MidgardProfiler.init();
            
            // Initialize integrations
            economyProvider = new VaultIntegration();
            
            // Initialize Region Provider
            if (plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
                try {
                    RegionManager.getInstance().setProvider(new WorldGuardIntegration());
                    plugin.getLogger().info("Integração com WorldGuard habilitada.");
                } catch (Throwable e) {
                     plugin.getLogger().warning("Falha ao integrar com WorldGuard: " + e.getMessage());
                }
            }
            loaded = true;
        } catch (Exception e) {
             plugin.getLogger().severe("Erro crítico na inicialização do MidgardCore: " + e.getMessage());
             e.printStackTrace();
             loaded = false; // Ensure it stays marked as failed
        }
    }
    
    public static boolean isLoaded() {
        return loaded;
    }

    public static JavaPlugin getInstance() {
        return pluginInstance;
   }

    public static void setPlaceholderRegistry(PlaceholderRegistry registry) {
        placeholderRegistry = registry;
    }
    
    public static PlaceholderRegistry getPlaceholderRegistry() {
        return placeholderRegistry;
    }

    public static JavaPlugin getPlugin() {
        return pluginInstance;
    }

    public static void setNMSHandler(NMSHandler handler) {
        nmsHandler = handler;
    }
    
    public static void setModuleManager(ModuleManager manager) {
        moduleManager = manager;
    }
    
    public static ModuleManager getModuleManager() {
        return moduleManager;
    }
    
    /**
     * Define o registro de comandos administrativos.
     * 
     * @param registry Registro de comandos admin
     */
    public static void setAdminCommand(AdminCommandRegistry registry) {
        adminCommandRegistry = registry;
    }
    
    /**
     * Obtém o registro de comandos administrativos.
     * Permite que módulos registrem subcomandos de admin.
     * 
     * @return Registro de comandos admin
     */
    public static AdminCommandRegistry getAdminCommand() {
        return adminCommandRegistry;
    }


    public static InventoryProtectionManager getInventoryProtectionManager() {
        return inventoryProtectionManager;
    }
    public static NMSHandler getNMSHandler() {
        return nmsHandler;
    }
    
    /**
     * Obtém o provedor de economia.
     *
     * @return Provedor de economia.
     */
    public static EconomyProvider getEconomyProvider() {
        return economyProvider;
    }
    
    /**
     * Obtém o gerenciador de perfis.
     *
     * @return Gerenciador de perfis.
     */
    public static ProfileManager getProfileManager() {
        return profileManager;
    }
    
    /**
     * Obtém o gerenciador de idiomas.
     *
     * @return Gerenciador de idiomas.
     */
    public static LanguageManager getLanguageManager() {
        return languageManager;
    }
    
    /**
     * Obtém o gerenciador de comandos.
     *
     * @return Gerenciador de comandos.
     */
    public static CommandManager getCommandManager() {
        return commandManager;
    }

    public static me.ray.midgard.core.loot.LootManager getLootManager() {
        return lootManager;
    }

    /**
     * Encerra o núcleo e libera recursos.
     */
    public static void shutdown() {
        try {
            if (economyProvider != null && economyProvider instanceof VaultIntegration) {
                // Cleanup if needed
            }
        } catch (Exception e) {
            // Ignore errors during shutdown cleanup
        }
        economyProvider = null;
        profileManager = null;
        languageManager = null;
        commandManager = null;
        lootManager = null;
        loaded = false;
    }
}
