package me.ray.midgard.modules.item.listener;

import me.ray.midgard.core.text.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

public class ChatInputListener implements Listener {

    private final JavaPlugin plugin;
    private static final Map<UUID, Consumer<String>> inputs = new HashMap<>();

    public ChatInputListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static void requestInput(Player player, Consumer<String> callback) {
        inputs.put(player.getUniqueId(), callback);
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        inputs.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.LOWEST)
    @SuppressWarnings("deprecation")
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (inputs.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            Consumer<String> callback = inputs.remove(player.getUniqueId());
            
            String message = event.getMessage();
            if (message.equalsIgnoreCase("cancel")) {
                MessageUtils.send(player, "<yellow>[Midgard] <red>Operação cancelada.");
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    callback.accept(message);
                } catch (Exception e) {
                     plugin.getLogger().log(Level.SEVERE, "Erro ao processar entrada de chat para o jogador " + player.getName(), e);
                     MessageUtils.send(player, "<red>Ocorreu um erro ao processar sua entrada.");
                }
            });
        }
    }
}
