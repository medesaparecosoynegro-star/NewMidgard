package me.ray.midgard.loader.command;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.attribute.AttributeInstance;
import me.ray.midgard.core.attribute.CoreAttributeData;
import me.ray.midgard.core.command.MidgardCommand;
import me.ray.midgard.core.profile.MidgardProfile;
import me.ray.midgard.core.text.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StatsCommand extends MidgardCommand {

    public StatsCommand() {
        super("stats", "midgard.command.stats", false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player target;

        if (args.length > 0) {
            if (!sender.hasPermission("midgard.admin.stats")) {
                MessageUtils.send(sender, "<red>Você não tem permissão para ver os atributos de outros jogadores.");
                return;
            }
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                MessageUtils.send(sender, "<red>Jogador não encontrado.");
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                MessageUtils.send(sender, "<red>Uso: /rpg stats <jogador>");
                return;
            }
            target = (Player) sender;
        }

        MidgardProfile profile = MidgardCore.getProfileManager().getProfile(target);
        if (profile == null) {
            MessageUtils.send(sender, "<red>Perfil não carregado.");
            return;
        }

        if (!profile.hasData(CoreAttributeData.class)) {
            MessageUtils.send(sender, "<red>No attribute data found for " + target.getName());
            return;
        }

        CoreAttributeData data = profile.getData(CoreAttributeData.class);
        
        if (sender instanceof Player player) {
            new me.ray.midgard.loader.gui.StatsGui(player, org.bukkit.plugin.java.JavaPlugin.getPlugin(me.ray.midgard.loader.MidgardPlugin.class), profile).open();
        } else {
            // Fallback for console
            MessageUtils.send(sender, "<gradient:#5e4fa2:#f79459><bold>Stats for " + target.getName() + "</bold></gradient>");
            
            for (Map.Entry<String, AttributeInstance> entry : data.getInstances().entrySet()) {
                AttributeInstance instance = entry.getValue();
                String icon = instance.getAttribute().getIcon();
                String format = instance.getAttribute().getFormat();
                
                String valueStr;
                try {
                    valueStr = new DecimalFormat(format).format(instance.getValue());
                } catch (Exception e) {
                    valueStr = String.valueOf(instance.getValue());
                }
                
                MessageUtils.send(sender, "<gray>• " + icon + " <yellow>" + instance.getAttribute().getName() + ": <white>" + valueStr);
            }
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return match(args[0], onlinePlayers());
        }
        return Collections.emptyList();
    }
}
