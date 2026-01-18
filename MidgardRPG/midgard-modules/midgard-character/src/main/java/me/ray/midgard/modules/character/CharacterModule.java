package me.ray.midgard.modules.character;

import me.ray.midgard.core.ModulePriority;
import me.ray.midgard.core.RPGModule;
import me.ray.midgard.modules.character.listener.HotbarListener;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class CharacterModule extends RPGModule {

    private static CharacterModule instance;
    private NamespacedKey compassKey;
    private FileConfiguration characterConfig;
    private FileConfiguration messagesConfig;
    private File configFile;

    public CharacterModule() {
        super("MidgardCharacter", ModulePriority.NORMAL);
    }

    public static CharacterModule getInstance() {
        return instance;
    }
    
    public FileConfiguration getCharacterConfig() {
        if (characterConfig == null) reloadCharacterConfig();
        return characterConfig;
    }
    
    @Override
    public void reloadConfig() {
        reloadCharacterConfig();
    }
    
    public void reloadCharacterConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "modules/character/config.yml");
        }
        
        if (!configFile.exists()) {
            if (configFile.getParentFile() != null && !configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }
            try (InputStream in = getClass().getResourceAsStream("/modules/character/config.yml")) {
                 if (in != null) {
                     Files.copy(in, configFile.toPath());
                 } else {
                     plugin.getLogger().warning("Recurso /modules/character/config.yml não encontrado no JAR.");
                 }
            } catch (Exception e) {
                 plugin.getLogger().log(Level.SEVERE, "Não foi possível salvar modules/character/config.yml", e);
            }
        }
        
        characterConfig = YamlConfiguration.loadConfiguration(configFile);
        
        try (InputStream in = getClass().getResourceAsStream("/modules/character/config.yml")) {
             if (in != null) {
                 characterConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(in, "UTF-8")));
                 characterConfig.options().copyDefaults(true);
                 characterConfig.save(configFile);
             }
        } catch (Exception e) { 
            plugin.getLogger().log(Level.WARNING, "Erro ao atualizar padrões da configuração de personagem.", e);
        }
    }

    @Override
    public void onEnable() {
        instance = this;
        this.compassKey = new NamespacedKey(plugin, "menu_compass");
        
        reloadCharacterConfig();
        loadMessages();
        
        HotbarListener listener = new HotbarListener(this);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        
        // Give compass to online players (reloads)
        try {
            for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
                if (player != null && player.isOnline()) {
                    try {
                        listener.giveCompass(player);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Falha ao entregar bússola de menu para " + player.getName());
                    }
                }
            }
        } catch (Exception e) {
             plugin.getLogger().log(Level.WARNING, "Erro ao iterar jogadores para entrega de itens iniciais.", e);
        }
        
        plugin.getLogger().info("MidgardCharacter habilitado! Modulo de personagem carregado.");
    }

    @Override
    public void onDisable() {
        
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public NamespacedKey getCompassKey() {
        return compassKey;
    }
    
    private void loadMessages() {
        File file = new File(plugin.getDataFolder(), "modules/character/messages/messages.yml");
        if (!file.exists()) {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (InputStream in = getClass().getResourceAsStream("/modules/character/messages/messages.yml")) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                } else {
                    plugin.getLogger().warning("Recurso /modules/character/messages/messages.yml não encontrado no JAR.");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Não foi possível salvar modules/character/messages/messages.yml", e);
            }
        }
        messagesConfig = YamlConfiguration.loadConfiguration(file);
    }

    public String getMessage(String path) {
        if (messagesConfig == null) return path;
        String msg = messagesConfig.getString(path);
        return msg != null ? msg.replace("&", "§") : path;
    }
    
    public List<String> getMessageList(String path) {
        if (messagesConfig == null) return Collections.emptyList();
        List<String> list = messagesConfig.getStringList(path);
        if (list == null) return Collections.emptyList();
        
        List<String> colored = new ArrayList<>();
        for (String line : list) {
            colored.add(line.replace("&", "§"));
        }
        return colored;
    }
}
