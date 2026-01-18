package me.ray.midgard.modules.combat.command;

import me.ray.midgard.core.command.MidgardCommand;
import me.ray.midgard.core.text.MessageUtils;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CombatDummyCommand extends MidgardCommand {

    private static final List<String> DAMAGE_TYPES = Arrays.asList(
            "ATTACK_DAMAGE", "WEAPON_DAMAGE", "PHYSICAL_DAMAGE", "MAGIC_DAMAGE",
            "PROJECTILE_DAMAGE", "SKILL_DAMAGE", "UNDEAD_DAMAGE",
            "FIRE_DAMAGE", "ICE_DAMAGE", "LIGHT_DAMAGE", "DARKNESS_DAMAGE", "DIVINE_DAMAGE"
    );

    public CombatDummyCommand() {
        super("dummy", "midgard.admin.dummy", true);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            MessageUtils.send(sender, "<red>Usage: /rpg dummy <create|clear> [type]");
            return;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            int count = 0;
            for (Entity entity : ((Player)sender).getWorld().getEntities()) {
                if (entity.getScoreboardTags().contains("midgard_dummy")) {
                    entity.remove();
                    count++;
                }
            }
            MessageUtils.send(sender, "<green>Removed " + count + " dummies.");
            return;
        }

        if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("spawn")) {
            if (args.length < 2) {
                MessageUtils.send(sender, "<red>Usage: /rpg dummy create <type>");
                return;
            }

            String type = args[1].toUpperCase();
            if (!type.endsWith("_DAMAGE")) {
                type += "_DAMAGE";
            }

            Player player = (Player) sender;
            Location loc = player.getLocation();

            try {
                String displayName = "<red><bold>" + type.replace("_DAMAGE", "") + " Dummy";

                if (!DAMAGE_TYPES.contains(type)) {
                     MessageUtils.send(sender, "<yellow>Warning: Unknown damage type '" + type + "'. Spawning anyway.");
                }

                Zombie zombie = (Zombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
                zombie.setAI(false);
                zombie.customName(MessageUtils.parse(displayName + " <dark_gray>[<green>||||||||||||||||||||<dark_gray>]"));
                zombie.setCustomNameVisible(true);
                zombie.setCanPickupItems(false);
                zombie.setAdult();

                // Infinite Health & Stats
                double maxHealth = 1024.0; // Max allowed by default Spigot config
                org.bukkit.attribute.AttributeInstance healthAttr = zombie.getAttribute(Attribute.MAX_HEALTH);
                if (healthAttr != null) {
                    healthAttr.setBaseValue(maxHealth);
                    zombie.setHealth(maxHealth);
                }

                org.bukkit.attribute.AttributeInstance kbAttr = zombie.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
                if (kbAttr != null) {
                    kbAttr.setBaseValue(1.0);
                }
                
                // Add tags for detection
                zombie.addScoreboardTag("midgard_dummy");
                zombie.addScoreboardTag("midgard.damage." + type);
                
                MessageUtils.send(sender, "<green>Spawned " + type + " Dummy.");

            } catch (Exception e) {
                MessageUtils.send(sender, "<red>Error spawning dummy: " + e.getMessage());
                me.ray.midgard.core.debug.MidgardLogger.error("Error spawning dummy", e);
            }
            return;
        }

        MessageUtils.send(sender, "<red>Usage: /rpg dummy <create|clear> [type]");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("create", "clear"), new ArrayList<>());
        } else if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            List<String> types = new ArrayList<>();
            for (String type : DAMAGE_TYPES) {
                types.add(type.replace("_DAMAGE", "").toLowerCase());
            }
            return StringUtil.copyPartialMatches(args[1], types, new ArrayList<>());
        }
        return Collections.emptyList();
    }
}
