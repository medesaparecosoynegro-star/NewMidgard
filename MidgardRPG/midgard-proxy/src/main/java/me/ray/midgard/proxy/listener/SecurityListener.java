package me.ray.midgard.proxy.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.ray.midgard.proxy.manager.SessionManager;

public class SecurityListener {

    private final SessionManager sessionManager;

    public SecurityListener(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Subscribe
    public void onServerSwitch(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        
        // Ignore initial connection (no current server)
        if (!player.getCurrentServer().isPresent()) return;

        // Check where they are going
        RegisteredServer target = event.getResult().getServer().orElse(null);
        if (target == null) return;
        
        // If targeting the same server (reconnect?), usually allowed or handled by Velocity
        if (player.getCurrentServer().get().getServer().equals(target)) return;

        // Check for safe token
        if (sessionManager.isSafe(player.getUniqueId())) {
            sessionManager.setSafe(player.getUniqueId(), false); // Consume token
            return;
        }

        // Halt and Sync
        event.setResult(ServerPreConnectEvent.ServerResult.denied());
        
        sessionManager.requestSave(player.getUniqueId()).thenAccept(success -> {
            sessionManager.setSafe(player.getUniqueId(), true);
            
            player.createConnectionRequest(target).connect().thenAccept(result -> {
                if (!result.isSuccessful()) {
                   // Connection failed after sync
                   // Maybe notify?
                }
            });
            
            if (!success) {
                // Warning if timeout occurred
                // player.sendMessage(Component.text("Sincronização lenta...", NamedTextColor.GRAY));
            }
        });
    }
}
