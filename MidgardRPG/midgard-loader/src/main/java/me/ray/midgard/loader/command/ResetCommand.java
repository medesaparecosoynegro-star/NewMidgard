package me.ray.midgard.loader.command;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.command.MidgardCommand;
import me.ray.midgard.core.profile.MidgardProfile;
import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.modules.classes.ClassData;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ResetCommand extends MidgardCommand {

    public ResetCommand() {
        super("reset", "midgard.admin.reset", false);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            MessageUtils.send(sender, "<red>Usage: /rpg reset <player>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            MessageUtils.send(sender, "<red>Player not found.");
            return;
        }

        MidgardProfile profile = MidgardCore.getProfileManager().getProfile(target);
        if (profile == null) {
            MessageUtils.send(sender, "<red>Profile not loaded.");
            return;
        }

        // Reset Class Data
        if (profile.hasData(ClassData.class)) {
            ClassData data = profile.getData(ClassData.class);
            data.setClassName(null);
            data.setLevel(1);
            data.setExperience(0);
            
            // Save immediately to avoid issues if server crashes
            MidgardCore.getProfileManager().saveProfile(profile);
        }
        
        MessageUtils.send(sender, "<green>Reset class data for " + target.getName());
        MessageUtils.send(target, "<yellow>Your class has been reset by an admin.");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return match(args[0], onlinePlayers());
        }
        return Collections.emptyList();
    }
}
