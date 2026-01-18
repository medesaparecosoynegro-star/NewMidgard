package me.ray.midgard.modules.classes;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.ModulePriority;
import me.ray.midgard.core.RPGModule;
import me.ray.midgard.core.attribute.Attribute;
import me.ray.midgard.core.attribute.AttributeInstance;
import me.ray.midgard.core.attribute.AttributeRegistry;
import me.ray.midgard.core.attribute.CoreAttributeData;
import me.ray.midgard.core.event.PlayerLevelUpEvent;
import me.ray.midgard.core.profile.MidgardProfile;
import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.modules.classes.gui.ClassSelectionGui;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Módulo de Classes do MidgardRPG.
 * Gerencia classes, atributos base e seleção de classe.
 */
public class ClassesModule extends RPGModule implements Listener {

    private static ClassesModule instance;
    private ClassManager classManager;
    private FileConfiguration messagesConfig;

    /**
     * Construtor do módulo de classes.
     */
    public ClassesModule() {
        super("Classes", ModulePriority.NORMAL);
    }

    @Override
    public void onEnable() {
        try {
            instance = this;
            loadMessages();
            // this.plugin is already set by super
            this.classManager = new ClassManager(plugin);
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            
            try {
                // Registra class command apenas no AdminCommand para /rpg admin class
                if (MidgardCore.getAdminCommand() != null) {
                    MidgardCore.getAdminCommand().registerSubcommand(new ClassCommand(this));
                }
                
                // Attributes é comando de jogador, fica no CommandManager principal
                AttributesCommand attributesCommand = new AttributesCommand(this);
                MidgardCore.getCommandManager().registerCommand(attributesCommand);
            } catch (Exception e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Falha ao registrar comandos do módulo de Classes", e);
            }
            
            // Register default attributes if they don't exist
            registerDefaultAttributes();
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro fatal ao habilitar módulo de Classes", e);
        }
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (classManager != null) {
            try {
                classManager.reload();
            } catch (Exception e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao recarregar configurações de Classes", e);
            }
        }
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static ClassesModule getInstance() {
        return instance;
    }

    public ClassManager getClassManager() {
        return classManager;
    }

    private void registerDefaultAttributes() {
        try {
            // We should probably load these from a config too, but for now let's ensure the ones used in classes.yml exist
            String[] defaults = {"strength", "defense", "vitality", "intelligence", "dexterity"};
            for (String id : defaults) {
                if (AttributeRegistry.getInstance().getAttribute(id) == null) {
                    // Create a default attribute
                    // We need to know how to create attributes. Attribute constructor?
                    // Attribute(id, name, base, min, max)
                    Attribute attr = new Attribute(id, capitalize(id), 0, 0, 1000);
                    AttributeRegistry.getInstance().register(id, attr);
                }
            }
        } catch (Exception e) {
             plugin.getLogger().log(java.util.logging.Level.WARNING, "Erro ao registrar atributos padrão", e);
        }
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @EventHandler
    public void onLevelUp(PlayerLevelUpEvent event) {
        try {
            Player player = event.getPlayer();
            if (player == null) return;
            
            MidgardProfile profile = MidgardCore.getProfileManager().getProfile(player);
            if (profile == null) return;
            
            ClassData data = profile.getData(ClassData.class);
            if (data != null && data.hasClass()) {
                // Sync level
                data.setLevel(event.getNewLevel());
                
                // Award points (e. g., 2 points per level)
                int pointsPerLevel = plugin.getConfig().getInt("modules.classes.points-per-level", 2);
                data.addAttributePoints(data.getAttributePoints() + pointsPerLevel);
                
                // Send progression messages
                String className = data.getClassName();
                if (classManager != null) {
                    RPGClass c = classManager.getClass(className);
                    if (c != null) className = c.getDisplayName();
                }
                
                String levelUpMsg = getMessage("progression.level_up")
                    .replace("%level%", String.valueOf(event.getNewLevel()))
                    .replace("%class%", className)
                    .replace("%class_name%", className);
                MessageUtils.send(player, levelUpMsg);
                
                String pointsMsg = getMessage("attributes.points_received")
                    .replace("%points%", String.valueOf(pointsPerLevel))
                    .replace("%total%", String.valueOf(data.getAttributePoints()));
                MessageUtils.send(player, pointsMsg);
                
                // Recalculate attributes
                if (classManager != null) {
                    RPGClass rpgClass = classManager.getClass(data.getClassName());
                    if (rpgClass != null) {
                        applyClassAttributes(profile, rpgClass, data.getLevel());
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao processar LevelUp para o jogador", e);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try {
            Player player = event.getPlayer();
            if (player == null) return;
            MidgardProfile profile = MidgardCore.getProfileManager().getProfile(player);
            
            if (profile == null) {
                // Not necessarily severe, but worth noting if profile system failed
                plugin.getLogger().warning("Perfil não encontrado para o jogador " + player.getName());
                return;
            }

            ClassData data = profile.getOrCreateData(ClassData.class);
            
            if (data.hasClass()) {
                if (classManager != null) {
                    RPGClass rpgClass = classManager.getClass(data.getClassName());
                    if (rpgClass != null) {
                        applyClassAttributes(profile, rpgClass, data.getLevel());
                    } else {
                         plugin.getLogger().warning("Classe '" + data.getClassName() + "' não encontrada para o jogador " + player.getName());
                    }
                }
            } else {
                // Open selection GUI if no class
                // Delay slightly to ensure client is ready
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        String welcomeMsg = getMessage("gui.opening_selection");
                        MessageUtils.send(player, welcomeMsg);
                        new ClassSelectionGui(plugin, player, this).open();
                    }
                }, 20L);
            }
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao processar onJoin no ClassesModule", e);
        }
    }

    /**
     * Aplica os atributos da classe ao perfil do jogador.
     *
     * @param profile Perfil do jogador.
     * @param rpgClass Classe RPG.
     * @param level Nível da classe.
     */
    public void applyClassAttributes(MidgardProfile profile, RPGClass rpgClass, int level) {
        if (profile == null || rpgClass == null) return;
        try {
            CoreAttributeData attrData = profile.getOrCreateData(CoreAttributeData.class);
            ClassData classData = profile.getData(ClassData.class);
            
            // Collect all relevant attributes (from class definition and spent points)
            java.util.Set<String> attributesToUpdate = new java.util.HashSet<>();
            if (rpgClass.getBaseAttributes() != null) {
                attributesToUpdate.addAll(rpgClass.getBaseAttributes().keySet());
            }
            if (classData != null && classData.getSpentPoints() != null) {
                attributesToUpdate.addAll(classData.getSpentPoints().keySet());
            }

            // Calculate stats
            for (String attrId : attributesToUpdate) {
                double base = rpgClass.getBaseAttributes().getOrDefault(attrId, 0.0);
                double perLevel = rpgClass.getAttributesPerLevel().getOrDefault(attrId, 0.0);
                
                double total = base + (perLevel * (level - 1));
                
                // Add spent points
                if (classData != null) {
                    total += classData.getSpentPoints(attrId); // Assuming 1 point = 1 value for now
                }
                
                AttributeInstance instance = attrData.getInstance(attrId);
                if (instance == null) {
                    // Try to register if missing? Or just skip?
                    // Safe to skip if attribute doesn't exist in registry, avoiding crash
                    // But ideally we should ensure it exists.
                    continue; 
                }
                
                instance.setBaseValue(total);
            }
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao aplicar atributos da classe", e);
        }
        
        // TODO: Apply Health and Mana (need specific handling for those as they might not be standard attributes)
    }
    
    private void loadMessages() {
        File file = new File(plugin.getDataFolder(), "modules/classes/messages/messages.yml");
        if (!file.exists()) {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (InputStream in = getClass().getResourceAsStream("/modules/classes/messages/messages.yml")) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                } else {
                    plugin.getLogger().warning("Recurso /modules/classes/messages/messages.yml não encontrado no JAR.");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Não foi possível salvar modules/classes/messages/messages.yml", e);
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
