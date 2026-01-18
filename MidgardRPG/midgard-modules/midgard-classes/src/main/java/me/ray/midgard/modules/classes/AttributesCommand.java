package me.ray.midgard.modules.classes;

import me.ray.midgard.core.command.MidgardCommand;
import me.ray.midgard.modules.classes.gui.AttributeDistributionGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class AttributesCommand extends MidgardCommand {

    private final ClassesModule module;

    public AttributesCommand(ClassesModule module) {
        super("attributes", null, true); // No permission required, player only
        this.module = module;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        new AttributeDistributionGui(player, module).open();
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
