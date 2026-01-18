package me.ray.midgard.modules.spells.command;

import me.ray.midgard.core.command.MidgardCommand;
import me.ray.midgard.modules.spells.SpellsModule;
import me.ray.midgard.modules.spells.gui.MainSpellGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkillsCommand extends MidgardCommand {

    private final SpellsModule module;

    public SkillsCommand(SpellsModule module) {
        super("skills", null, true);
        this.module = module;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        new MainSpellGUI(player, module).open();
    }
}
