package me.ray.midgard.modules.spells.command;

import me.ray.midgard.core.MidgardCore; // Added import
import me.ray.midgard.core.command.MidgardCommand;
import me.ray.midgard.modules.spells.SpellsModule;
import me.ray.midgard.modules.spells.data.SpellProfile;
import me.ray.midgard.modules.spells.gui.MainSpellGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpellCommand extends MidgardCommand {

    private SpellsModule module;
    private final List<MidgardCommand> subCommands = new ArrayList<>();

    public SpellCommand(SpellsModule module) {
        // Name: "spell", Permission: null (for now), PlayerOnly: true
        super("spell", null, true);
        this.module = module;
    }

    public void addSubCommand(MidgardCommand cmd) {
        subCommands.add(cmd);
    }
    
    private SpellsModule getModule() {
        if (module != null && module.getSpellManager() != null) {
            return module;
        }
        // Try to recover module reference
        try {
            return (SpellsModule) MidgardCore.getModuleManager().getModule("Spells");
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        SpellsModule m = getModule();
        if (m == null) return Collections.emptyList();

        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.add("help");
            options.add("bind");
            options.add("combo");
            options.add("list");
            
            for (MidgardCommand sub : subCommands) {
                options.add(sub.getName());
            }
            return StringUtil.copyPartialMatches(args[0], options, new ArrayList<>());
        }
        
        // Delegate to subcommand
        String subName = args[0];
        for (MidgardCommand cmd : subCommands) {
            if (cmd.getName().equalsIgnoreCase(subName)) {
                String[] newArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
                // Manually check permission before offering tab completion
                if (cmd.getPermission() != null && !sender.hasPermission(cmd.getPermission())) {
                    return Collections.emptyList();
                }
                return cmd.tabComplete(sender, newArgs);
            }
        }
        
        if (args[0].equalsIgnoreCase("bind")) {
             if (args.length == 2) {
                 List<String> slots = java.util.Arrays.asList("1", "2", "3", "4", "5", "6");
                 return StringUtil.copyPartialMatches(args[1], slots, new ArrayList<>());
             }
             if (args.length == 3) {
                 return StringUtil.copyPartialMatches(args[2], new ArrayList<>(m.getSpellManager().getLoadedSpellIds()), new ArrayList<>());
             }
        }
        
        if (args[0].equalsIgnoreCase("combo")) {
              if (args.length == 2) {
                 List<String> suggestions = java.util.Arrays.asList("L", "R", "LL", "RR", "RL", "LR");
                 return StringUtil.copyPartialMatches(args[1], suggestions, new ArrayList<>());
              }
              if (args.length == 3) {
                  return StringUtil.copyPartialMatches(args[2], new ArrayList<>(m.getSpellManager().getLoadedSpellIds()), new ArrayList<>());
              }
        }

        return Collections.emptyList();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Handle Subcommands
        if (args.length > 0) {
            String sub = args[0];
            for (MidgardCommand cmd : subCommands) {
                if (cmd.getName().equalsIgnoreCase(sub)) {
                    if (cmd.getPermission() != null && !cmd.getPermission().isEmpty() && !sender.hasPermission(cmd.getPermission())) {
                        me.ray.midgard.core.text.MessageUtils.send(sender, module.getMessage("errors.no_permission"));
                        return;
                    }
                    // Shift args
                    String[] newArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
                    cmd.execute(sender, newArgs);
                    return;
                }
            }
        }
        
        Player player = (Player) sender;
        SpellsModule m = getModule();
        if (m == null || m.getSpellManager() == null) {
            me.ray.midgard.core.text.MessageUtils.send(player, module.getMessage("errors.system_error"));
            return;
        }

        if (args.length == 0) {
            new MainSpellGUI(player, m).open();
            return;
        }

        if (args[0].equalsIgnoreCase("help")) {
            for (String line : module.getMessageList("commands.help_lines")) {
                me.ray.midgard.core.text.MessageUtils.send(player, line);
            }
            return;
        }

        if (args[0].equalsIgnoreCase("bind")) {
            if (args.length < 3) {
                me.ray.midgard.core.text.MessageUtils.send(player, module.getMessage("commands.usage_bind"));
                return;
            }
            try {
                int slot = Integer.parseInt(args[1]);
                String spellId = args[2];
                
                if (m.getSpellManager().getSpell(spellId) == null) {
                    String notFoundMsg = module.getMessage("errors.spell_not_found")
                        .replace("%spell%", spellId);
                    me.ray.midgard.core.text.MessageUtils.send(player, notFoundMsg);
                    return;
                }

                SpellProfile profile = m.getSpellManager().getProfile(player);
                if (profile == null) {
                   me.ray.midgard.core.text.MessageUtils.send(player, module.getMessage("errors.profile_not_loaded"));
                   return;
                }
                
                profile.setSkillBarSlot(slot, spellId);
                String boundMsg = module.getMessage("commands.spell_bound")
                    .replace("%spell%", spellId)
                    .replace("%slot%", String.valueOf(slot));
                me.ray.midgard.core.text.MessageUtils.send(player, boundMsg);

            } catch (NumberFormatException e) {
                me.ray.midgard.core.text.MessageUtils.send(player, module.getMessage("errors.invalid_slot"));
            }
            return;
        }

        if (args[0].equalsIgnoreCase("combo")) {
            if (args.length < 3) {
                me.ray.midgard.core.text.MessageUtils.send(player, module.getMessage("commands.usage_combo"));
                return;
            }
            String combo = args[1].toUpperCase();
            String spellId = args[2];

            if (m.getSpellManager().getSpell(spellId) == null) {
                String notFoundMsg = module.getMessage("errors.spell_not_found")
                    .replace("%spell%", spellId);
                me.ray.midgard.core.text.MessageUtils.send(player, notFoundMsg);
                return;
            }

            SpellProfile profile = m.getSpellManager().getProfile(player);
            if (profile == null) {
                me.ray.midgard.core.text.MessageUtils.send(player, module.getMessage("errors.profile_not_loaded"));
                return;
            }

            profile.setComboLegacy(combo, spellId);
            String comboMsg = module.getMessage("commands.combo_bound")
                .replace("%spell%", spellId)
                .replace("%combo%", combo);
            me.ray.midgard.core.text.MessageUtils.send(player, comboMsg);
            return;
        }
    }
}
    

