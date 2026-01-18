package me.ray.midgardProxy.config;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private final Path dataDirectory;
    private final Logger logger;
    private CommentedConfigurationNode rootNode;
    private final String fileName = "config.yml";

    @Inject
    public ConfigManager(@DataDirectory Path dataDirectory, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    public void load() {
        try {
            if (Files.notExists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            Path configFile = dataDirectory.resolve(fileName);
            ConfigurationLoader<CommentedConfigurationNode> loader = YamlConfigurationLoader.builder()
                    .path(configFile)
                    .nodeStyle(NodeStyle.BLOCK)
                    .build();

            if (Files.notExists(configFile)) {
                rootNode = loader.createNode();
                saveDefaults(rootNode);
                loader.save(rootNode);
                logger.info("Configuration file created: " + fileName);
            } else {
                rootNode = loader.load();
                logger.info("Configuration loaded.");
            }
        } catch (IOException e) {
            logger.error("Failed to load configuration", e);
        }
    }

    private void saveDefaults(CommentedConfigurationNode node) throws IOException {
        // Redis
        node.node("redis", "enabled").set(false); // Default to false to avoid crash on start
        node.node("redis", "host").set("localhost");
        node.node("redis", "port").set(6379);
        node.node("redis", "password").set("");

        // Lobby
        node.node("lobby", "servers").setList(String.class, java.util.List.of("lobby-1", "lobby"));
        node.node("lobby", "fallback").set(true);
        
        // Extra Servers (Example)
        node.node("servers", "survival").set("survival-1");
        node.node("servers", "pvp").set("arena-1");

        // Messages
        node.node("messages", "prefix").set("<gradient:blue:aqua>[Midgard]</gradient> ");
        node.node("messages", "no_lobby_available").set("<red>No lobby server is currently available.");
        node.node("messages", "connecting_lobby").set("<green>Connecting to lobby...");
    }

    public String getRedisHost() {
        return rootNode.node("redis", "host").getString("localhost");
    }

    public int getRedisPort() {
        return rootNode.node("redis", "port").getInt(6379);
    }

    public String getRedisPassword() {
        return rootNode.node("redis", "password").getString("");
    }
    
    public boolean isRedisEnabled() {
        return rootNode.node("redis", "enabled").getBoolean(false);
    }
    
    public java.util.List<String> getLobbyServers() {
        try {
            return rootNode.node("lobby", "servers").getList(String.class);
        } catch (Exception e) {
            return java.util.List.of("lobby-1");
        }
    }

    public boolean isLobbyFallbackEnabled() {
        return rootNode.node("lobby", "fallback").getBoolean(true);
    }
    
    public String getMessage(String key) {
        return rootNode.node("messages", key).getString("");
    }
    
    public String getPrefix() {
        return rootNode.node("messages", "prefix").getString("");
    }
}
