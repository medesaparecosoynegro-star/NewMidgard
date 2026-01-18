package me.ray.midgard.modules.combat.mechanics;

import me.ray.midgard.core.attribute.AttributeInstance;
import me.ray.midgard.core.attribute.CoreAttributeData;
import me.ray.midgard.modules.combat.CombatAttributes;
import me.ray.midgard.modules.combat.RPGDamageCategory;
import me.ray.midgard.modules.combat.RPGDamageContext;
import org.bukkit.entity.Player;

public class ThornsMechanic {

    public void apply(Player attacker, double damage, double elementalDamage, CoreAttributeData victimAttributes, RPGDamageContext context) {
        if (attacker == null) return;

        AttributeInstance thornsAttr = victimAttributes.getInstance(CombatAttributes.THORNS);
        double thorns = thornsAttr != null ? thornsAttr.getValue() : 0.0;

        if (thorns > 0 && (context.hasCategory(RPGDamageCategory.PHYSICAL) || context.hasCategory(RPGDamageCategory.PROJECTILE))) {
            double reflected = (damage + elementalDamage) * (thorns / 100.0);
            if (reflected > 0) {
                attacker.damage(reflected);
            }
        }
    }
}
