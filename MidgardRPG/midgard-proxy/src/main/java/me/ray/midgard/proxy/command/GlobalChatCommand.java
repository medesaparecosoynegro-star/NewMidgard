package me.ray.midgard.proxy.command;

import com.google.gson.JsonObject;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.ray.midgard.proxy.redis.ProxyRedisManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class GlobalChatCommand implements SimpleCommand {

    private final ProxyRedisManager redisManager;

    public GlobalChatCommand(ProxyRedisManager redisManager) {
        this.redisManager = redisManager;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) {
            invocation.source().sendMessage(Component.text("Apenas jogadores."));
            return;
        }

        Player player = (Player) invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            player.sendMessage(Component.text("Uso: /g <mensagem>", NamedTextColor.RED));
            return;
        }

        if (!redisManager.isEnabled()) {
            player.sendMessage(Component.text("Chat global indisponível no momento.", NamedTextColor.RED));
            return;
        }

        String message = String.join(" ", args);
        
        // Publish to Redis
        JsonObject json = new JsonObject();
        json.addProperty("sender", player.getUsername());
        json.addProperty("msg", message);
        json.addProperty("server", player.getCurrentServer().map(s -> s.getServerInfo().getName()).orElse("Unknown"));

        redisManager.publish("midgard:global_chat", json.toString());
    }
}
