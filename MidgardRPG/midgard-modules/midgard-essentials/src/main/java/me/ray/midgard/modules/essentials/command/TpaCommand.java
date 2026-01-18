package me.ray.midgard.modules.essentials.command;

import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.modules.essentials.manager.EssentialsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class TpaCommand extends EssentialsBaseCommand {

    public TpaCommand(EssentialsManager manager) {
        super(manager, "tpa", "midgard.essentials.tpa", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0) {
            MessageUtils.send(player, "&cUso: /tpa <jogador>");
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            MessageUtils.send(player, "&cJogador não encontrado.");
            return;
        }

        if (target.equals(player)) {
            MessageUtils.send(player, manager.getMessage("tpa.self"));
            return;
        }

        manager.getTeleportRequestManager().sendRequest(player, target);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return match(args[0], onlinePlayers());
        }
        return Collections.emptyList();
    }
}
