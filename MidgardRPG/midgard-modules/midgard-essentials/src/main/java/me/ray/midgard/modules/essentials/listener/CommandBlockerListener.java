package me.ray.midgard.modules.essentials.listener;

import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.modules.essentials.manager.EssentialsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandBlockerListener implements Listener {

    private final EssentialsManager manager;

    public CommandBlockerListener(EssentialsManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer().hasPermission("midgard.essentials.bypass.blockedcmds")) {
            return;
        }

        List<String> blockedCommands = manager.getConfig().getConfig().getStringList("blocked-commands");
        String message = manager.getConfig().getConfig().getString("messages.command-blocked", "&cVocê não tem permissão para usar este comando.");

        // O comando vem como "/comando args...", pegamos apenas a primeira parte e convertemos para minúsculo
        String command = event.getMessage().split(" ")[0].toLowerCase();

        for (String blocked : blockedCommands) {
            // Normaliza o comando bloqueado para minúsculo
            String blockedCmd = blocked.toLowerCase();

            // Verifica correspondência exata (ex: /pl)
            if (command.equals(blockedCmd)) {
                event.setCancelled(true);
                MessageUtils.send(event.getPlayer(), message);
                return;
            }
            
            // Verifica comandos com namespace (ex: /bukkit:pl ou /about:)
            // Se o usuário configurou "/about:", ele quer bloquear qualquer coisa que comece com isso?
            // Ou se ele configurou "/pl", ele quer bloquear "/bukkit:pl"?
            
            // Lógica simples: Se o comando configurado for encontrado
            if (command.equalsIgnoreCase(blockedCmd)) {
                 event.setCancelled(true);
                 MessageUtils.send(event.getPlayer(), message);
                 return;
            }
        }
    }
}
