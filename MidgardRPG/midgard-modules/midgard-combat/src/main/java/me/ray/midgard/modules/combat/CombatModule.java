package me.ray.midgard.modules.combat;

import me.ray.midgard.modules.combat.level.LevelListener;
import me.ray.midgard.modules.combat.level.LevelManager;
import me.ray.midgard.modules.combat.listener.DummyListener;
import me.ray.midgard.core.ModulePriority;
import me.ray.midgard.core.RPGModule;
import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.modules.combat.command.CombatDummyCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Módulo de Combate do MidgardRPG.
 * <p>
 * Este módulo é responsável por gerenciar toda a lógica de combate do RPG, incluindo:
 * <ul>
 *     <li>Registro e gerenciamento de atributos de combate (Vida, Mana, Stamina, etc).</li>
 *     <li>Sistema de indicadores de dano (Hologramas).</li>
 *     <li>Gerenciamento de regeneração e stamina.</li>
 *     <li>Sistema de níveis e experiência.</li>
 * </ul>
 */
public class CombatModule extends RPGModule {

    private static CombatModule instance;
    private FileConfiguration messagesConfig;
    private LevelManager levelManager; // Stored field

    /**
     * Construtor do módulo de combate.
     * Inicializa o módulo com o nome "MidgardCombat".
     */
    public CombatModule() {
        super("MidgardCombat", ModulePriority.HIGH);
    }
    
    public static CombatModule getInstance() {
        return instance;
    }
    
    public LevelManager getLevelManager() {
        return levelManager;
    }

    /**
     * Chamado quando o módulo é habilitado.
     * Inicializa configurações, atributos, overlay, indicadores de dano, gerenciador de combate e sistema de níveis.
     */
    @Override
    public void onEnable() {
        instance = this;
        loadMessages();
        plugin.getLogger().info("Lógica de combate inicializada! Espadas estão afiadas.");
        
        CombatConfig config = new CombatConfig(plugin);
        CombatAttributes.register();
        CombatPlaceholders.register();
        new CombatOverlay(plugin).start();
        
        // Inicializa o Sistema de Indicadores de Dano
        DamageIndicatorManager indicatorManager = new DamageIndicatorManager(plugin, config);
        // plugin.getServer().getPluginManager().registerEvents(new DamageListener(indicatorManager, config), plugin);
        
        new CombatManager(plugin, config, indicatorManager).start();

        // Inicializa o Sistema de Níveis
        this.levelManager = new LevelManager(config);
        plugin.getServer().getPluginManager().registerEvents(new LevelListener(levelManager, config), plugin);
        plugin.getServer().getPluginManager().registerEvents(new DummyListener(), plugin);

        // Registra dummy command apenas no AdminCommand para /rpg admin dummy
        if (MidgardCore.getAdminCommand() != null) {
            MidgardCore.getAdminCommand().registerSubcommand(new CombatDummyCommand());
        }
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (CombatManager.getInstance() != null && CombatManager.getInstance().getConfig() != null) {
            CombatManager.getInstance().getConfig().reload();
        }
    }

    /**
     * Chamado quando o módulo é desabilitado.
     * Realiza a limpeza necessária.
     */
    @Override
    public void onDisable() {
        instance = null;
        plugin.getLogger().info("Lógica de combate desabilitada.");
    }
    
    private void loadMessages() {
        File file = new File(plugin.getDataFolder(), "modules/combat/messages/messages.yml");
        if (!file.exists()) {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            try (InputStream in = getClass().getResourceAsStream("/modules/combat/messages/messages.yml")) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                } else {
                    plugin.getLogger().warning("Recurso /modules/combat/messages/messages.yml não encontrado no JAR.");
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Não foi possível salvar modules/combat/messages/messages.yml", e);
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
