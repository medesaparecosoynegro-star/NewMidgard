package me.ray.midgard.modules.essentials.manager;

import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.modules.essentials.config.EssentialsConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {

    private final JavaPlugin plugin;
    private final EssentialsManager essentialsManager;
    private final Set<UUID> vanishedPlayers;

    public VanishManager(JavaPlugin plugin, EssentialsConfig config, EssentialsManager essentialsManager) {
        this.plugin = plugin;
        this.essentialsManager = essentialsManager;
        this.vanishedPlayers = new HashSet<>();
    }

    public void toggleVanish(Player player) {
        if (isVanished(player)) {
            unvanish(player);
        } else {
            vanish(player);
        }
    }

    public void vanish(Player player) {
        vanishedPlayers.add(player.getUniqueId());
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.hasPermission("midgard.vanish.see")) {
                onlinePlayer.hidePlayer(plugin, player);
            }
        }
        MessageUtils.send(player, essentialsManager.getMessage("vanish.enabled"));
    }

    public void unvanish(Player player) {
        vanishedPlayers.remove(player.getUniqueId());
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.showPlayer(plugin, player);
        }
        MessageUtils.send(player, essentialsManager.getMessage("vanish.disabled"));
    }

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }
    
    public void updateFor(Player player) {
        if (!player.hasPermission("midgard.vanish.see")) {
            for (UUID uuid : vanishedPlayers) {
                Player vanishedPlayer = Bukkit.getPlayer(uuid);
                if (vanishedPlayer != null) {
                    player.hidePlayer(plugin, vanishedPlayer);
                }
            }
        }
    }
    
    public void removePlayer(Player player) {
        vanishedPlayers.remove(player.getUniqueId());
    }
}
