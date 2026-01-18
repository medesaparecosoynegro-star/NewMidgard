package me.ray.midgard.modules.essentials.manager;

import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.modules.essentials.config.EssentialsConfig;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportRequestManager {

    private final JavaPlugin plugin;
    private final EssentialsConfig config;
    private final EssentialsManager essentialsManager;
    // Target UUID -> Sender UUID
    private final Map<UUID, UUID> requests = new HashMap<>();
    // Sender UUID -> Task ID (for expiration)
    private final Map<UUID, Integer> tasks = new HashMap<>();

    public TeleportRequestManager(JavaPlugin plugin, EssentialsConfig config, EssentialsManager essentialsManager) {
        this.plugin = plugin;
        this.config = config;
        this.essentialsManager = essentialsManager;
    }

    public void sendRequest(Player sender, Player target) {
        if (requests.containsKey(target.getUniqueId()) && requests.get(target.getUniqueId()).equals(sender.getUniqueId())) {
            MessageUtils.send(sender, essentialsManager.getMessage("tpa.already_sent"));
            return;
        }

        requests.put(target.getUniqueId(), sender.getUniqueId());
        
        // Expire after 60 seconds
        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (requests.get(target.getUniqueId()) != null && requests.get(target.getUniqueId()).equals(sender.getUniqueId())) {
                    requests.remove(target.getUniqueId());
                    if (sender.isOnline()) {
                        MessageUtils.send(sender, essentialsManager.getMessage("tpa.expired"));
                    }
                }
                tasks.remove(sender.getUniqueId());
            }
        }.runTaskLater(plugin, 20L * 60).getTaskId();
        
        tasks.put(sender.getUniqueId(), taskId);

        MessageUtils.send(sender, essentialsManager.getMessage("tpa.sent").replace("%player%", target.getName()));
        MessageUtils.send(target, essentialsManager.getMessage("tpa.received").replace("%player%", sender.getName()));
    }

    public void acceptRequest(Player target) {
        UUID senderId = requests.remove(target.getUniqueId());
        if (senderId == null) {
            MessageUtils.send(target, essentialsManager.getMessage("tpa.no_request"));
            return;
        }

        Player sender = plugin.getServer().getPlayer(senderId);
        if (sender != null && sender.isOnline()) {
            cancelTask(senderId);
            sender.teleport(target);
            MessageUtils.send(sender, essentialsManager.getMessage("tpa.teleporting"));
            MessageUtils.send(target, essentialsManager.getMessage("tpa.accepted"));
        } else {
            MessageUtils.send(target, "&cO jogador que enviou a solicitação não está mais online.");
        }
    }

    public void denyRequest(Player target) {
        UUID senderId = requests.remove(target.getUniqueId());
        if (senderId == null) {
            MessageUtils.send(target, essentialsManager.getMessage("tpa.no_request"));
            return;
        }

        Player sender = plugin.getServer().getPlayer(senderId);
        if (sender != null && sender.isOnline()) {
            cancelTask(senderId);
            MessageUtils.send(sender, essentialsManager.getMessage("tpa.denied"));
        }
        MessageUtils.send(target, "&cSolicitação negada.");
    }

    private void cancelTask(UUID senderId) {
        if (tasks.containsKey(senderId)) {
            plugin.getServer().getScheduler().cancelTask(tasks.remove(senderId));
        }
    }
}
