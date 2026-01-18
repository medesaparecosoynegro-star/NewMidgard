package me.ray.midgard.modules.essentials.command;

import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.modules.essentials.manager.EssentialsManager;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HomeCommand extends EssentialsBaseCommand {

    public HomeCommand(EssentialsManager manager) {
        super(manager, "home", "midgard.essentials.home", true);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length == 0) {
            // If only one home, teleport to it. If multiple, list them.
            // Or default to "home" if exists.
            // Essentials behavior: /home teleports to "home" if exists, or lists if not specified and multiple?
            // Let's stick to: if no args, try "home". If not found, list.
            
            Location defaultHome = manager.getHomeManager().getHome(player, "home");
            if (defaultHome != null) {
                player.teleport(defaultHome);
                MessageUtils.send(player, manager.getMessage("home.teleport").replace("%home%", "home"));
                return;
            }
            
            String homes = String.join(", ", manager.getHomeManager().getHomes(player));
            MessageUtils.send(player, manager.getMessage("home.list_header"));
            return;
        }

        String homeName = args[0];
        Location home = manager.getHomeManager().getHome(player, homeName);

        if (home == null) {
            MessageUtils.send(player, manager.getMessage("home.not_found"));
            return;
        }

        player.teleport(home);
        MessageUtils.send(player, manager.getMessage("home.teleport").replace("%home%", homeName));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (sender instanceof Player && args.length == 1) {
            return match(args[0], manager.getHomeManager().getHomes((Player) sender));
        }
        return super.tabComplete(sender, args);
    }
}
