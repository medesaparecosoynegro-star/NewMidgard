package me.ray.midgard.modules.spells.integration;

import me.ray.midgard.modules.spells.api.ResourceProvider;
import org.bukkit.entity.Player;

public class DummyResourceProvider implements ResourceProvider {

    @Override
    public double getMana(Player player) {
        return 0.0;
    }

    @Override
    public double getStamina(Player player) {
        return 0.0;
    }

    @Override
    public boolean consumeMana(Player player, double amount) {
        return true; // Infinite resources in dummy mode
    }

    @Override
    public boolean consumeStamina(Player player, double amount) {
        return true;
    }
}
