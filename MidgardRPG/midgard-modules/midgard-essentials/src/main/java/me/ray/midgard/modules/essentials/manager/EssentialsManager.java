package me.ray.midgard.modules.essentials.manager;

import me.ray.midgard.modules.essentials.config.EssentialsConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;

public class EssentialsManager {

    private final JavaPlugin plugin;
    private final EssentialsConfig config;
    private final WarpManager warpManager;
    private final SpawnManager spawnManager;
    private final HomeManager homeManager;
    private final TeleportRequestManager teleportRequestManager;
    private final VanishManager vanishManager;
    private final TeleportHistoryManager teleportHistoryManager;
    private FileConfiguration messagesConfig;

    public EssentialsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        // Initialize config first (risky if file errors)
        try {
            this.config = new EssentialsConfig(plugin);
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro crítico ao carregar configurações do Essentials", e);
            throw new RuntimeException("Falha na inicialização do EssentialsManager", e); 
        }

        // Initialize managers safely
        this.warpManager = safeInit(() -> new WarpManager(config), "WarpManager", plugin);
        this.spawnManager = safeInit(() -> new SpawnManager(config), "SpawnManager", plugin);
        this.homeManager = safeInit(() -> new HomeManager(plugin, config), "HomeManager", plugin);
        this.teleportHistoryManager = safeInit(() -> new TeleportHistoryManager(), "TeleportHistoryManager", plugin);
        
        // Load messages before VanishManager and TeleportRequestManager since they need EssentialsManager reference
        loadMessages();
        
        this.teleportRequestManager = safeInit(() -> new TeleportRequestManager(plugin, config, this), "TeleportRequestManager", plugin);
        this.vanishManager = safeInit(() -> new VanishManager(plugin, config, this), "VanishManager", plugin);
    }
    
    private <T> T safeInit(java.util.function.Supplier<T> supplier, String name, JavaPlugin plugin) {
        try {
            return supplier.get();
        } catch (Exception e) {
             plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao inicializar " + name, e);
             return null;
        }
    }

    public EssentialsConfig getConfig() {
        return config;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public TeleportRequestManager getTeleportRequestManager() {
        return teleportRequestManager;
    }

    public VanishManager getVanishManager() {
        return vanishManager;
    }

    public TeleportHistoryManager getTeleportHistoryManager() {
        return teleportHistoryManager;
    }
    
    private void loadMessages() {
        try {
            File messagesFile = new File(plugin.getDataFolder(), "modules/essentials/messages/messages.yml");
            if (!messagesFile.exists()) {
                plugin.saveResource("modules/essentials/messages/messages.yml", false);
            }
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
            plugin.getLogger().info("Mensagens do Essentials carregadas com sucesso!");
        } catch (Exception e) {
            plugin.getLogger().warning("Erro ao carregar mensagens do Essentials: " + e.getMessage());
        }
    }
    
    public String getMessage(String path) {
        if (messagesConfig == null) return "";
        String message = messagesConfig.getString(path, "");
        return message.replace("&", "§");
    }
    
    public List<String> getMessageList(String path) {
        if (messagesConfig == null) return java.util.Collections.emptyList();
        List<String> messages = messagesConfig.getStringList(path);
        return messages.stream()
            .map(msg -> msg.replace("&", "§"))
            .collect(java.util.stream.Collectors.toList());
    }
}
