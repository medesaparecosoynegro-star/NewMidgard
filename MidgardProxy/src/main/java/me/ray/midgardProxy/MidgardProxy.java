package me.ray.midgardProxy;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import me.ray.midgardProxy.command.GlobalChatCommand;
import me.ray.midgardProxy.command.LobbyCommand;
import me.ray.midgardProxy.command.ReloadCommand;
import me.ray.midgardProxy.config.ConfigManager;
import me.ray.midgardProxy.listener.SwitchListener;
import me.ray.midgardProxy.redis.RedisManager;
import me.ray.midgardProxy.redis.RedisSubscriber;
import org.slf4j.Logger;

@Plugin(id = "midgardproxy", name = "MidgardProxy", version = BuildConstants.VERSION)
public class MidgardProxy {

    private final ProxyServer server;
    private final Logger logger;
    private final ConfigManager configManager;
    private RedisManager redisManager;

    @Inject
    public MidgardProxy(ProxyServer server, Logger logger, ConfigManager configManager) {
        this.server = server;
        this.logger = logger;
        this.configManager = configManager;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        configManager.load();
        
        CommandManager commandManager = server.getCommandManager();

        // Initialize Redis only if enabled
        if (configManager.isRedisEnabled()) {
            try {
                this.redisManager = new RedisManager(configManager);
                RedisSubscriber subscriber = new RedisSubscriber(server, configManager, redisManager);
                redisManager.subscribe(subscriber, "midgard:global_chat", "midgard:sync:saved");
                server.getEventManager().register(this, new SwitchListener(redisManager, logger));
                
                CommandMeta gMeta = commandManager.metaBuilder("global")
                    .aliases("g")
                    .plugin(this)
                    .build();
                commandManager.register(gMeta, new GlobalChatCommand(redisManager));
                logger.info("Redis system initialized successfully.");
            } catch (Exception e) {
                logger.error("Failed to initialize Redis system. Disabling Redis features.", e);
            }
        } else {
            logger.info("Redis is disabled in config. Skipping Redis initialization.");
        }

        CommandMeta lobbyMeta = commandManager.metaBuilder("lobby")
                .aliases("hub", "l")
                .plugin(this)
                .build();
        commandManager.register(lobbyMeta, new LobbyCommand(server, configManager));

        CommandMeta mpMeta = commandManager.metaBuilder("midgardproxy")
                .aliases("mp")
                .plugin(this)
                .build();
        commandManager.register(mpMeta, new ReloadCommand(configManager));

        logger.info("MidgardProxy initialized with configuration and commands!");
    }
    
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (redisManager != null) {
            redisManager.close();
            logger.info("Redis connection closed.");
        }
    }
    
    public ProxyServer getServer() {
        return server;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
}
