package me.ray.midgard.modules.classes;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.command.MidgardCommand;
import me.ray.midgard.core.profile.MidgardProfile;
import me.ray.midgard.core.text.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Comando administrativo para gerenciamento de classes.
 * <p>
 * Permite definir a classe de um jogador manualmente.
 * Uso: /rpg class set <classe>
 */
public class ClassCommand extends MidgardCommand {

    private final ClassesModule module;

    /**
     * Construtor do ClassCommand.
     *
     * @param module Instância do módulo de classes.
     */
    public ClassCommand(ClassesModule module) {
        super("class", "midgard.admin.class", true);
        this.module = module;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        
        // Admin command to set class for self or others (simplified for now)
        if (args.length < 2 || !args[0].equalsIgnoreCase("set")) {
            MessageUtils.send(player, module.getMessage("commands.usage_main"));
            return;
        }

        String classId = args[1].toLowerCase();
        RPGClass rpgClass = module.getClassManager().getClass(classId);

        if (rpgClass == null) {
            String msg = module.getMessage("errors.class_not_found")
                .replace("%class%", classId);
            MessageUtils.send(player, msg);
            return;
        }

        MidgardProfile profile = MidgardCore.getProfileManager().getProfile(player);
        if (profile == null) {
            MessageUtils.send(player, module.getMessage("errors.profile_error"));
            return;
        }

        ClassData data = profile.getOrCreateData(ClassData.class);
        String oldClass = data.hasClass() ? data.getClassName() : "";
        data.setClassName(classId);
        data.setLevel(1); // Reset level on class change? For now, yes.
        data.setExperience(0);
        
        // Apply attributes immediately
        module.applyClassAttributes(profile, rpgClass, 1);
        
        String msg;
        if (oldClass.isEmpty()) {
            msg = module.getMessage("class.selected")
                .replace("%class%", rpgClass.getDisplayName())
                .replace("%class_name%", rpgClass.getDisplayName());
        } else {
            msg = module.getMessage("class.changed")
                .replace("%old_class%", oldClass)
                .replace("%new_class%", rpgClass.getDisplayName());
        }
        MessageUtils.send(player, msg);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Collections.singletonList("set"), new ArrayList<>());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return StringUtil.copyPartialMatches(args[1], module.getClassManager().getClasses().keySet(), new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
