package me.ray.midgard.loader;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.ModuleManager;
import me.ray.midgard.core.attribute.Attribute;
import me.ray.midgard.core.attribute.AttributeRegistry;
import me.ray.midgard.core.config.ConfigWrapper;
import me.ray.midgard.core.database.DatabaseCredentials;
import me.ray.midgard.core.database.DatabaseManager;
import me.ray.midgard.core.gui.GuiListener;
import me.ray.midgard.core.i18n.LanguageManager;
import me.ray.midgard.core.leaderboard.LeaderboardManager;
import me.ray.midgard.core.placeholder.PlaceholderRegistry;
import me.ray.midgard.core.profile.ProfileManager;
import me.ray.midgard.core.redis.RedisCredentials;
import me.ray.midgard.core.redis.RedisManager;
import me.ray.midgard.core.utils.CooldownManager;
import me.ray.midgard.core.utils.PDCUtils;
import me.ray.midgard.core.utils.Task;
import me.ray.midgard.loader.command.AdminCommand;
import me.ray.midgard.loader.command.StatsCommand;
import me.ray.midgard.loader.listener.ItemMechanicsListener;
import me.ray.midgard.loader.listener.MobDebugListener;
import me.ray.midgard.loader.listener.StatUpdateListener;
import me.ray.midgard.modules.character.CharacterModule;
import me.ray.midgard.modules.classes.ClassesModule;
import me.ray.midgard.modules.combat.CombatModule;
import me.ray.midgard.modules.essentials.EssentialsModule;
import me.ray.midgard.modules.item.ItemModule;
import me.ray.midgard.modules.performance.PerformanceModule;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Level;

public class MidgardBootstrap {

    private final MidgardPlugin plugin;
    
    // Config
    private ConfigWrapper mainConfig;

    // Managers
    private DatabaseManager databaseManager;
    private RedisManager redisManager;
    private LanguageManager languageManager;
    private CooldownManager cooldownManager;
    private ProfileManager profileManager;
    private ModuleManager moduleManager;
    private LeaderboardManager leaderboardManager;
    private PlaceholderRegistry placeholderRegistry;

    public MidgardBootstrap(MidgardPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        plugin.getLogger().info("Iniciando processo de bootstrap do MidgardRPG...");

        // 1. Utils & Core
        initUtils();

        // 2. Configurações
        this.mainConfig = new ConfigWrapper(plugin, "config.yml");
        
        // Garante que todas as pastas de recursos (modules, messages, examples) existam
        new ResourceInstaller(plugin).install();

        // loadDebugSettings(); // Removido
        loadAttributes();

        // 3. Persistência (DB & Redis)
        initPersistence(this.mainConfig);

        // 4. Idioma
        initLanguage();

        // 5. Managers Principais
        initCoreManagers();

        // 6. Integrações
        initIntegrations();

        // 7. Configuração do Core Estático
        initStaticCore();

        // 8. Comandos Base
        registerBaseCommands();

        // 9. Módulos
        initModules();
        
        plugin.getLogger().info("Bootstrap concluído com sucesso!");
    }

    public void shutdown() {
        if (moduleManager != null) moduleManager.disableAll();
        if (profileManager != null) profileManager.shutdown();
        if (redisManager != null) redisManager.shutdown();
        if (databaseManager != null) databaseManager.shutdown();
        MidgardCore.shutdown();
    }

    private void initUtils() {
        Task.init(plugin);
        PDCUtils.init(plugin);
        this.cooldownManager = new CooldownManager();
    }

    private void loadAttributes() {
        ConfigWrapper attributesConfig = new ConfigWrapper(plugin, "settings/attributes.yml");
        ConfigurationSection section = attributesConfig.getConfig().getConfigurationSection("attributes");
        
        if (section != null) {
            int count = 0;
            for (String key : section.getKeys(false)) {
                ConfigurationSection attrSection = section.getConfigurationSection(key);
                if (attrSection == null) continue;
                
                String name = attrSection.getString("name", key);
                String icon = attrSection.getString("icon", "");
                String format = attrSection.getString("format", "0.0");
                double base = attrSection.getDouble("base", 0.0);
                double min = attrSection.getDouble("min", 0.0);
                double max = attrSection.getDouble("max", 100000.0);
                
                Attribute attribute = new Attribute(key, name, base, min, max, icon, format);
                AttributeRegistry.getInstance().register(key, attribute);
                count++;
            }
            plugin.getLogger().info("Carregados " + count + " atributos.");
        }
    }

    private void initPersistence(ConfigWrapper config) {
        // Database
        try {
            this.databaseManager = new DatabaseManager(plugin);
            DatabaseCredentials credentials = new DatabaseCredentials(
                config.getConfig().getString("database.type", "sqlite"),
                config.getConfig().getString("database.host", "localhost"),
                config.getConfig().getInt("database.port", 3306),
                config.getConfig().getString("database.database", "midgard"),
                config.getConfig().getString("database.username", "root"),
                config.getConfig().getString("database.password", "password"),
                config.getConfig().getBoolean("database.use-ssl", false)
            );
            this.databaseManager.initialize(credentials);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "ERRO CRÍTICO: Falha no Banco de Dados. O plugin será desativado.", e);
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            throw new RuntimeException("Database init failed", e);
        }

        // Redis (Opcional)
        try {
            this.redisManager = new RedisManager(plugin);
            if (config.getConfig().getBoolean("redis.enabled", false)) {
                RedisCredentials credentials = new RedisCredentials(
                    config.getConfig().getString("redis.host", "localhost"),
                    config.getConfig().getInt("redis.port", 6379),
                    config.getConfig().getString("redis.password", ""),
                    config.getConfig().getBoolean("redis.use-ssl", false)
                );
                this.redisManager.initialize(credentials);
                this.leaderboardManager = new LeaderboardManager(redisManager);
                plugin.getLogger().info("Redis conectado.");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Falha ao conectar no Redis (funcionalidade opcional).", e);
        }
    }

    private void initLanguage() {
        this.languageManager = new LanguageManager(plugin);
        this.languageManager.load("ignored");
    }

    private void initCoreManagers() {
        plugin.getServer().getPluginManager().registerEvents(new GuiListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new StatUpdateListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new ItemMechanicsListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new MobDebugListener(), plugin);
        
        this.profileManager = new ProfileManager(plugin, databaseManager, redisManager);
        
        // Sync Listener
        if (redisManager != null && redisManager.isEnabled()) {
            me.ray.midgard.core.redis.SyncReqListener syncListener = new me.ray.midgard.core.redis.SyncReqListener(profileManager, redisManager);
            redisManager.subscribe("midgard:sync:req_save", syncListener);
        }
    }

    private void initIntegrations() {
        if (plugin.getServer().getPluginManager().isPluginEnabled("MythicMobs")) {
            plugin.getServer().getPluginManager().registerEvents(new me.ray.midgard.modules.mythicmobs.MythicMobsModule(), plugin);
            
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getLogger().info("Recarregando MythicMobs para aplicar mecânicas...");
                org.bukkit.Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), "mm reload");
            }, 20L);
        }

        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.placeholderRegistry = new PlaceholderRegistry(plugin);
            this.placeholderRegistry.register();
            MidgardCore.setPlaceholderRegistry(this.placeholderRegistry);
        }
    }

    private void initStaticCore() {
        try {
            String version = plugin.getServer().getBukkitVersion();
            if (version.contains("1.21")) {
                me.ray.midgard.nms.api.NMSHandler nmsHandler = new me.ray.midgard.nms.v1_21.NMSHandlerImpl();
                MidgardCore.setNMSHandler(nmsHandler);
                plugin.getLogger().info("NMS Handler carregado para versão 1.21");
            } else {
                plugin.getLogger().severe("Versão do servidor não suportada: " + version + ". Funcionalidades NMS serão desativadas. Apenas 1.21 é suportada.");
            }
        } catch (Throwable e) {
            plugin.getLogger().log(Level.SEVERE, "Erro crítico ao carregar NMS Handler!", e);
        }
        
        MidgardCore.init(plugin, profileManager, languageManager);
        plugin.getCommand("midgardrpg").setExecutor(MidgardCore.getCommandManager());
        plugin.getCommand("midgardrpg").setTabCompleter(MidgardCore.getCommandManager());
    }

    private void registerBaseCommands() {
        // Comando principal de admin - agrupa todos os subcomandos administrativos
        AdminCommand adminCommand = new AdminCommand(plugin);
        MidgardCore.getCommandManager().registerCommand(adminCommand);
        MidgardCore.setAdminCommand(adminCommand);
        
        // Stats ainda pode ser acessado diretamente para jogadores verem seus próprios stats
        MidgardCore.getCommandManager().registerCommand(new StatsCommand());
    }

    private void initModules() {
        this.moduleManager = new ModuleManager(plugin);
        MidgardCore.setModuleManager(this.moduleManager);
        
        // Helper method to safely register modules
        registerSafely("combat", () -> new CombatModule());
        registerSafely("essentials", () -> new EssentialsModule());
        registerSafely("classes", () -> new ClassesModule());
        registerSafely("item", () -> new ItemModule());
        registerSafely("character", () -> new CharacterModule());
        registerSafely("spells", () -> new me.ray.midgard.modules.spells.SpellsModule());
        registerSafely("performance", () -> new PerformanceModule());
        
        moduleManager.enableAll();
    }
    
    private void registerSafely(String name, java.util.function.Supplier<me.ray.midgard.core.RPGModule> moduleSupplier) {
        if (shouldLoadModule(name)) {
            try {
                moduleManager.registerModule(moduleSupplier.get());
            } catch (Throwable e) {
                 plugin.getLogger().log(Level.SEVERE, "Falha ao registrar módulo: " + name + ". Ele será desativado.", e);
            }
        }
    }

    private boolean shouldLoadModule(String moduleName) {
        return mainConfig.getConfig().getBoolean("modules." + moduleName, true);
    }

    // Getters
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public RedisManager getRedisManager() { return redisManager; }
    public LanguageManager getLanguageManager() { return languageManager; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public ProfileManager getProfileManager() { return profileManager; }
    public LeaderboardManager getLeaderboardManager() { return leaderboardManager; }
    public PlaceholderRegistry getPlaceholderRegistry() { return placeholderRegistry; }
    public me.ray.midgard.core.ModuleManager getModuleManager() { return moduleManager; }
}
