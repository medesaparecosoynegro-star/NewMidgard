package me.ray.midgard.modules.combat;

import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import me.ray.midgard.core.MidgardCore;

@SuppressWarnings("deprecation")
public class DamageOverrideContext {
    private static final String ELEMENT_KEY = "midgard_forced_element";
    private static final String TYPE_KEY = "midgard_forced_type";
    private static final String DAMAGE_KEY = "midgard_forced_damage";

    public static void setForcedElement(Entity entity, String element) {
        entity.setMetadata(ELEMENT_KEY, new FixedMetadataValue(MidgardCore.getPlugin(), element));
    }

    public static String getForcedElement(Entity entity) {
        if (entity.hasMetadata(ELEMENT_KEY)) {
            return entity.getMetadata(ELEMENT_KEY).get(0).asString();
        }
        return null;
    }

    public static void setForcedType(Entity entity, String type) {
        entity.setMetadata(TYPE_KEY, new FixedMetadataValue(MidgardCore.getPlugin(), type));
    }

    public static String getForcedType(Entity entity) {
        if (entity.hasMetadata(TYPE_KEY)) {
            return entity.getMetadata(TYPE_KEY).get(0).asString();
        }
        return null;
    }

    public static void setForcedDamage(Entity entity, double damage) {
        entity.setMetadata(DAMAGE_KEY, new FixedMetadataValue(MidgardCore.getPlugin(), damage));
        // System.out.println("[DamageOverrideContext] Set forced damage: " + damage + " for " + entity.getName());
    }

    public static Double getForcedDamage(Entity entity) {
        if (entity.hasMetadata(DAMAGE_KEY)) {
            // System.out.println("[DamageOverrideContext] Get forced damage raw: " + val + " (" + (val != null ? val.getClass().getName() : "null") + ")");
            return entity.getMetadata(DAMAGE_KEY).get(0).asDouble();
        }
        // System.out.println("[DamageOverrideContext] No forced damage metadata found for " + entity.getName());
        return null;
    }

    public static void clear(Entity entity) {
        if (entity.hasMetadata(ELEMENT_KEY)) {
            entity.removeMetadata(ELEMENT_KEY, MidgardCore.getPlugin());
        }
        if (entity.hasMetadata(TYPE_KEY)) {
            entity.removeMetadata(TYPE_KEY, MidgardCore.getPlugin());
        }
        if (entity.hasMetadata(DAMAGE_KEY)) {
            entity.removeMetadata(DAMAGE_KEY, MidgardCore.getPlugin());
        }
    }
}
