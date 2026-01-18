package me.ray.midgard.modules.spells;

import me.ray.midgard.core.RPGModule;
import me.ray.midgard.core.command.BukkitCommandWrapper;
import me.ray.midgard.core.utils.CommandRegisterUtils;
import me.ray.midgard.modules.spells.command.SpellCommand;
import me.ray.midgard.modules.spells.command.SkillsCommand;
import me.ray.midgard.modules.spells.listener.SpellsListener;
import me.ray.midgard.modules.spells.api.ResourceProvider;
import me.ray.midgard.modules.spells.integration.CombatModuleBridge;
import me.ray.midgard.modules.spells.integration.DummyResourceProvider;
import me.ray.midgard.modules.spells.manager.SpellManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

import me.ray.midgard.modules.spells.task.SkillBarTask;

public class SpellsModule extends RPGModule {

    private SpellManager spellManager;
    private SpellsListener spellsListener;
    private ResourceProvider resourceProvider;
    private YamlConfiguration messagesConfig;

    public SpellsModule() {
        super("Spells");
    }

    @Override
    public void onEnable() {
        loadMessages();
        
        setupResourceProvider();
        
        this.spellManager = new SpellManager(this);
        this.spellManager.loadSpells();

        this.spellsListener = new SpellsListener(this);
        Bukkit.getPluginManager().registerEvents(this.spellsListener, getPlugin());
        
        // Register MythicMobs Integration
        if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            getPlugin().getLogger().info("SpellsModule: MythicMobs integration enabled.");
        }
        
        // Register /spell
        SpellCommand spellCmd = new SpellCommand(this);
        CommandRegisterUtils.register(getPlugin(), new BukkitCommandWrapper(spellCmd.getName(), spellCmd));
        
        // Register /skills
        SkillsCommand skillsCmd = new SkillsCommand(this);
        CommandRegisterUtils.register(getPlugin(), new BukkitCommandWrapper(skillsCmd.getName(), skillsCmd)); // Register as main command /skills

        // Comandos spell e skills já são registrados como comandos diretos (/spell, /skills)
        // Não precisam estar no /midgard - são comandos de jogador
        
        // Start SkillBar Task
        new SkillBarTask(this).runTaskTimer(getPlugin(), 0L, 5L);
        
        getPlugin().getLogger().info("SpellsModule enabled!");
    }

    @Override
    public void onDisable() {
        // Cleanup if needed
        this.spellManager = null;
    }

    private void setupResourceProvider() {
        boolean combatLoaded = false;
        try {
            combatLoaded = me.ray.midgard.core.MidgardCore.getModuleManager().getModule("MidgardCombat") != null;
        } catch (Exception e) {
            // Fallback check
        }

        if (combatLoaded) {
            // Better check: Check if CombatData class is reachable
            try {
                Class.forName("me.ray.midgard.modules.combat.CombatData");
                this.resourceProvider = new CombatModuleBridge();
                me.ray.midgard.core.debug.MidgardLogger.info("SpellsModule: Hooked into Midgard-Combat for resources.");
            } catch (ClassNotFoundException e) {
                this.resourceProvider = new DummyResourceProvider();
                me.ray.midgard.core.debug.MidgardLogger.warn("SpellsModule: Combat module loaded but CombatData class not found. Using dummy resources.");
            }
        } else {
            this.resourceProvider = new DummyResourceProvider();
        }
    }

    public ResourceProvider getResourceProvider() {
        return resourceProvider;
    }

    public SpellManager getSpellManager() {
        return spellManager;
    }

    private void loadMessages() {
        File file = new File(getPlugin().getDataFolder(), "modules/spells/messages.yml");
        if (!file.exists()) {
            getPlugin().saveResource("modules/spells/messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(file);
    }

    public String getMessage(String path) {
        if (messagesConfig == null) return path;
        String msg = messagesConfig.getString(path);
        return msg != null ? msg : path;
    }
    
    public java.util.List<String> getMessageList(String path) {
        if (messagesConfig == null) return java.util.Collections.emptyList();
        java.util.List<String> list = messagesConfig.getStringList(path);
        return list != null ? list : java.util.Collections.emptyList();
    }

}
