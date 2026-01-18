package me.ray.midgard.modules.combat.mechanics;

import me.ray.midgard.core.attribute.AttributeInstance;
import me.ray.midgard.core.attribute.CoreAttributeData;
import me.ray.midgard.modules.combat.CombatAttributes;
import me.ray.midgard.modules.combat.DamageIndicatorManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.concurrent.ThreadLocalRandom;

public class ParryMechanic implements CombatMechanic {

    private final DamageIndicatorManager indicatorManager;

    public ParryMechanic(DamageIndicatorManager indicatorManager) {
        this.indicatorManager = indicatorManager;
    }

    @Override
    public boolean apply(EntityDamageEvent event, LivingEntity victim, CoreAttributeData victimAttributes) {
        AttributeInstance parryAttr = victimAttributes.getInstance(CombatAttributes.PARRY_RATING);
        double parryChance = parryAttr != null ? parryAttr.getValue() : 0.0;

        if (parryChance > 0 && ThreadLocalRandom.current().nextDouble() * 100 < parryChance) {
            indicatorManager.spawnCustomIndicator(victim, "PARRY", "§e");
            event.setCancelled(true);
            return true;
        }
        return false;
    }
}
