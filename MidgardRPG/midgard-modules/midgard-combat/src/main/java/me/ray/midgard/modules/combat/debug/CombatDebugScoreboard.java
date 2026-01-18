package me.ray.midgard.modules.combat.debug;

import me.ray.midgard.core.text.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatDebugScoreboard {

    private final Map<UUID, Scoreboard> playerScoreboards = new HashMap<>();
    private final Map<UUID, Scoreboard> previousScoreboards = new HashMap<>();

    // Legacy color codes mapping for unique team entries
    private static final String[] COLOR_CODES = new String[] {
        "§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7",
        "§8", "§9", "§a", "§b", "§c", "§d", "§e", "§f"
    };

    @SuppressWarnings("deprecation")
    public void enable(Player player) {
        if (playerScoreboards.containsKey(player.getUniqueId())) return;

        previousScoreboards.put(player.getUniqueId(), player.getScoreboard());
        
        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = sb.registerNewObjective("midgard_debug", "dummy", MessageUtils.parse("  <gold><bold>MIDGARD RPG  "));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Layout Profissional
        createLine(sb, "sep_top", 15, "<dark_gray><strikethrough>--------------------");
        createLine(sb, "header_combat", 14, "<gold>⚔ Info");
        createLine(sb, "attacker", 13, " <gray>Atk: ");
        createLine(sb, "cause", 12, " <gray>Src: ");
        createLine(sb, "spacer1", 11, "");
        createLine(sb, "header_damage", 10, "<gold>💥 Dano");
        createLine(sb, "element", 9, " <gray>Elem: ");
        createLine(sb, "forced", 8, " <gray>Base: ");
        createLine(sb, "damage", 7, " <gray>Final: ");
        createLine(sb, "spacer2", 6, "");
        createLine(sb, "header_details", 5, "<gold>📝 Detalhes");
        createLine(sb, "det1", 4, "");
        createLine(sb, "det2", 3, "");
        createLine(sb, "det3", 2, "");
        createLine(sb, "sep_bot", 1, "<dark_gray><strikethrough>--------------------");

        player.setScoreboard(sb);
        playerScoreboards.put(player.getUniqueId(), sb);
    }

    public void disable(Player player) {
        if (previousScoreboards.containsKey(player.getUniqueId())) {
            player.setScoreboard(previousScoreboards.get(player.getUniqueId()));
            previousScoreboards.remove(player.getUniqueId());
        }
        playerScoreboards.remove(player.getUniqueId());
    }

    public boolean isEnabled(Player player) {
        return playerScoreboards.containsKey(player.getUniqueId());
    }

    public void update(Player player, String attacker, String cause, String categories, String damage, String element, String forced, Map<String, Double> details) {
        Scoreboard sb = playerScoreboards.get(player.getUniqueId());
        if (sb == null) return;

        updateLine(sb, "attacker", "<white>" + limit(attacker, 16));
        updateLine(sb, "cause", "<white>" + limit(cause, 16));

        updateLine(sb, "element", "<aqua>" + element);
        updateLine(sb, "forced", "<white>" + forced);
        updateLine(sb, "damage", "<red><bold>" + damage);

        int i = 1;
        for (Map.Entry<String, Double> entry : details.entrySet()) {
            if (i > 3) break;
            String key = entry.getKey().replace("_damage", "");
            // Formata: " - Key: Val"
            updateLine(sb, "det" + i, " <gray>▪ " + limit(key, 10) + ": <white>" + String.format("%.1f", entry.getValue()));
            i++;
        }
        // Limpa linhas restantes
        for (; i <= 3; i++) {
            updateLine(sb, "det" + i, "");
        }
    }

    private void createLine(Scoreboard sb, String name, int score, String prefix) {
        Team team = sb.registerNewTeam(name);
        // Usa códigos de cor únicos para cada linha para não colidir
        String entry = COLOR_CODES[score];
        team.addEntry(entry);
        team.prefix(MessageUtils.parse(prefix));
        team.suffix(Component.empty());
        Objective obj = sb.getObjective(DisplaySlot.SIDEBAR);
        if (obj != null) {
            obj.getScore(entry).setScore(score);
        }
    }

    private void updateLine(Scoreboard sb, String name, String suffix) {
        Team team = sb.getTeam(name);
        if (team != null) {
            team.suffix(MessageUtils.parse(suffix));
        }
    }
    
    private String limit(String s, int l) {
        if (s == null || s.isEmpty()) return "-";
        return s.length() > l ? s.substring(0, l) : s;
    }
}
