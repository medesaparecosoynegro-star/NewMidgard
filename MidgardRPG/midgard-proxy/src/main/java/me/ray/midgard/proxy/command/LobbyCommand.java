package me.ray.midgard.proxy.command;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.ray.midgard.proxy.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class LobbyCommand implements SimpleCommand {

    private final ProxyServer server;
    private final ConfigManager configManager;

    public LobbyCommand(ProxyServer server, ConfigManager configManager) {
        this.server = server;
        this.configManager = configManager;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player)) {
            invocation.source().sendMessage(Component.text("Apenas jogadores podem usar este comando."));
            return;
        }

        Player player = (Player) invocation.source();
        List<String> lobbyServers = configManager.getLobbyServers();

        if (lobbyServers.isEmpty()) {
            player.sendMessage(Component.text("Nenhum servidor de lobby configurado.", NamedTextColor.RED));
            return;
        }

        // Try to find a lobby server
        // Simple random load balancing
        String targetName = lobbyServers.get(ThreadLocalRandom.current().nextInt(lobbyServers.size()));
        Optional<RegisteredServer> target = server.getServer(targetName);
        
        // If the preferred one is not present, iterate to find any valid one
        if (target.isEmpty()) {
             for (String name : lobbyServers) {
                 Optional<RegisteredServer> s = server.getServer(name);
                 if (s.isPresent()) {
                     target = s;
                     break;
                 }
             }
        }

        if (target.isPresent()) {
            RegisteredServer lobby = target.get();
            if (player.getCurrentServer().map(s -> s.getServer().equals(lobby)).orElse(false)) {
                player.sendMessage(Component.text("Você já está no lobby!", NamedTextColor.RED));
                return;
            }
            
            player.sendMessage(Component.text("Conectando ao lobby...", NamedTextColor.GRAY));
            player.createConnectionRequest(lobby).connect().thenAccept(result -> {
                if (!result.isSuccessful()) {
                   player.sendMessage(Component.text("Falha ao conectar: ", NamedTextColor.RED)
                           .append(result.getReasonComponent().orElse(Component.text("Motivo desconhecido"))));
                }
            });
        } else {
             player.sendMessage(Component.text("Servidor de Lobby não encontrado na rede.", NamedTextColor.RED));
        }
    }
}
