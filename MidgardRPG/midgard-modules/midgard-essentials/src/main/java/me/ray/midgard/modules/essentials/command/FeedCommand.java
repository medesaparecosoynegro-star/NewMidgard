package me.ray.midgard.modules.essentials.command;

import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.modules.essentials.manager.EssentialsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class FeedCommand extends EssentialsBaseCommand {

    public FeedCommand(EssentialsManager manager) {
        super(manager, "feed", "midgard.essentials.feed", false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player target;
        if (args.length > 0) {
            if (!sender.hasPermission("midgard.essentials.feed.others")) {
                MessageUtils.send(sender, "&cVocê não tem permissão para alimentar outros jogadores.");
                return;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                MessageUtils.send(sender, "&cJogador não encontrado.");
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                MessageUtils.send(sender, "&cConsole deve especificar um jogador.");
                return;
            }
            target = (Player) sender;
        }

        target.setFoodLevel(20);
        target.setSaturation(20);

        if (target.equals(sender)) {
            MessageUtils.send(sender, manager.getMessage("player.feed"));
        } else {
            MessageUtils.send(sender, manager.getMessage("player.feed_other")
                    .replace("%player%", target.getName()));
            MessageUtils.send(target, manager.getMessage("player.fed_by")
                    .replace("%player%", sender.getName()));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1 && sender.hasPermission("midgard.essentials.feed.others")) {
            return match(args[0], onlinePlayers());
        }
        return Collections.emptyList();
    }
}
