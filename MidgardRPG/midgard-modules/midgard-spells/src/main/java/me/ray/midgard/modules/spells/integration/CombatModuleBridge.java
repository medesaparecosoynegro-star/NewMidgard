package me.ray.midgard.modules.spells.integration;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.profile.MidgardProfile;
import me.ray.midgard.modules.combat.CombatData;
import me.ray.midgard.modules.spells.api.ResourceProvider;
import org.bukkit.entity.Player;

public class CombatModuleBridge implements ResourceProvider {

    @Override
    public double getMana(Player player) {
        MidgardProfile coreProfile = MidgardCore.getProfileManager().getProfile(player.getUniqueId());
        if (coreProfile == null) return 0.0;
        
        CombatData data = coreProfile.getData(CombatData.class);
        return (data != null) ? data.getCurrentMana() : 0.0;
    }

    @Override
    public double getStamina(Player player) {
        MidgardProfile coreProfile = MidgardCore.getProfileManager().getProfile(player.getUniqueId());
        if (coreProfile == null) return 0.0;
        
        CombatData data = coreProfile.getData(CombatData.class);
        return (data != null) ? data.getCurrentStamina() : 0.0;
    }

    @Override
    public boolean consumeMana(Player player, double amount) {
        MidgardProfile coreProfile = MidgardCore.getProfileManager().getProfile(player.getUniqueId());
        if (coreProfile == null) return false;
        
        CombatData data = coreProfile.getData(CombatData.class);
        if (data == null) return false;

        if (data.getCurrentMana() >= amount) {
            data.setCurrentMana(data.getCurrentMana() - amount);
            return true;
        }
        return false;
    }

    @Override
    public boolean consumeStamina(Player player, double amount) {
        MidgardProfile coreProfile = MidgardCore.getProfileManager().getProfile(player.getUniqueId());
        if (coreProfile == null) return false;
        
        CombatData data = coreProfile.getData(CombatData.class);
        if (data == null) return false;

        if (data.getCurrentStamina() >= amount) {
            data.setCurrentStamina(data.getCurrentStamina() - amount);
            return true;
        }
        return false;
    }
}
