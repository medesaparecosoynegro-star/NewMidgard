package me.ray.midgard.modules.essentials;

import me.ray.midgard.core.ModulePriority;
import me.ray.midgard.core.RPGModule;
import me.ray.midgard.modules.essentials.command.*;
import me.ray.midgard.modules.essentials.listener.TeleportListener;
import me.ray.midgard.modules.essentials.listener.VanishListener;
import me.ray.midgard.modules.essentials.manager.EssentialsManager;
import me.ray.midgard.modules.essentials.utils.CommandRegistry;
import org.bukkit.plugin.java.JavaPlugin;

public class EssentialsModule extends RPGModule {

    private EssentialsManager manager;

    public EssentialsModule() {
        super("MidgardEssentials", ModulePriority.NORMAL);
    }

    @Override
    public void onEnable() {
        try {
            this.manager = new EssentialsManager(plugin);
            
            // Example of using the new config system
            // boolean someSetting = getConfig().getBoolean("some-setting");
            
            registerCommands(plugin);
            registerListeners(plugin);
            
            plugin.getLogger().info("MidgardEssentials habilitado!");
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro fatal ao habilitar MidgardEssentials", e);
        }
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        try {
            if (manager != null) {
                if (manager.getConfig() != null) {
                     manager.getConfig().reload();
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao recarregar configurações do Essentials", e);
        }
    }

    @Override
    public void onDisable() {
        try {
             // Cleanup logic if needed
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao desabilitar MidgardEssentials", e);
        }
    }
    
    private void registerCommands(JavaPlugin plugin) {
        try {
            if (plugin == null) return;
            CommandRegistry.register(plugin, new GamemodeCommand(manager), "gm", "gmode");
            CommandRegistry.register(plugin, new FlyCommand(manager));
            CommandRegistry.register(plugin, new HealCommand(manager));
            CommandRegistry.register(plugin, new FeedCommand(manager), "eat");
            CommandRegistry.register(plugin, new SpawnCommand(manager));
            CommandRegistry.register(plugin, new SetSpawnCommand(manager));
            CommandRegistry.register(plugin, new WarpCommand(manager));
            CommandRegistry.register(plugin, new SetWarpCommand(manager));
            CommandRegistry.register(plugin, new DelWarpCommand(manager));
            CommandRegistry.register(plugin, new HomeCommand(manager));
            CommandRegistry.register(plugin, new SetHomeCommand(manager));
            CommandRegistry.register(plugin, new DelHomeCommand(manager));
            CommandRegistry.register(plugin, new TpaCommand(manager));
            CommandRegistry.register(plugin, new TpacceptCommand(manager));
            CommandRegistry.register(plugin, new TpdenyCommand(manager));
            CommandRegistry.register(plugin, new VanishCommand(manager), "v");
            CommandRegistry.register(plugin, new BackCommand(manager));
            CommandRegistry.register(plugin, new TeleportCommand(manager), "tp");
            CommandRegistry.register(plugin, new TeleportHereCommand(manager), "tphere", "s");
            CommandRegistry.register(plugin, new TopCommand(manager));
            CommandRegistry.register(plugin, new SpeedCommand(manager));
            CommandRegistry.register(plugin, new InvseeCommand(manager), "inv");
        } catch (Exception e) {
            if (plugin != null) plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao registrar comandos do Essentials", e);
        }
    }

    private void registerListeners(JavaPlugin plugin) {
        try {
            if (plugin == null) return;
            plugin.getServer().getPluginManager().registerEvents(new VanishListener(manager), plugin);
            plugin.getServer().getPluginManager().registerEvents(new TeleportListener(manager), plugin);
            plugin.getServer().getPluginManager().registerEvents(new me.ray.midgard.modules.essentials.listener.CommandBlockerListener(manager), plugin);
        } catch (Exception e) {
             if (plugin != null) plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao registrar listeners do Essentials", e);
        }
    }
}
