package me.ray.midgard.core.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class PlaceholderRegistry extends PlaceholderExpansion {

    @SuppressWarnings("unused")
    private final JavaPlugin plugin;
    private final Map<String, BiFunction<OfflinePlayer, String, String>> placeholders = new HashMap<>();

    public PlaceholderRegistry(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(String identifier, BiFunction<OfflinePlayer, String, String> handler) {
        placeholders.put(identifier.toLowerCase(), handler);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "midgard";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Ray";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        for (Map.Entry<String, BiFunction<OfflinePlayer, String, String>> entry : placeholders.entrySet()) {
            if (params.toLowerCase().startsWith(entry.getKey())) {
                String rest = params.substring(entry.getKey().length());
                if (rest.startsWith("_")) rest = rest.substring(1);
                return entry.getValue().apply(player, rest);
            }
        }
        return null;
    }
}
