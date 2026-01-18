package me.ray.midgard.modules.spells.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.ray.midgard.modules.spells.SpellsModule;

public class EditorListener implements Listener {

    private final SpellsModule module;
    private final Map<UUID, Consumer<String>> pendingInputs = new HashMap<>();

    public EditorListener(SpellsModule module) {
        this.module = module;
    }

    public void requestInput(Player player, Consumer<String> callback) {
        pendingInputs.put(player.getUniqueId(), callback);
    }
    
    public void cancelInput(Player player) {
        pendingInputs.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (pendingInputs.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            Consumer<String> callback = pendingInputs.remove(player.getUniqueId());
            
            // Run on main thread
            new BukkitRunnable() {
                @Override
                public void run() {
                    callback.accept(event.getMessage());
                }
            }.runTask(module.getPlugin());
        }
    }
}
