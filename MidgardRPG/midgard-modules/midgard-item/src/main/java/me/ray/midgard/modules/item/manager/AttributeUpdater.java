package me.ray.midgard.modules.item.manager;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.attribute.AttributeInstance;
import me.ray.midgard.core.attribute.AttributeModifier;
import me.ray.midgard.core.attribute.AttributeOperation;
import me.ray.midgard.core.attribute.CoreAttributeData;
import me.ray.midgard.core.profile.MidgardProfile;
import me.ray.midgard.modules.combat.CombatAttributes;
import me.ray.midgard.modules.item.ItemModule;
import me.ray.midgard.modules.item.model.ItemStat;
import me.ray.midgard.modules.item.model.MidgardItem;
import me.ray.midgard.modules.item.utils.ItemPDC;
import me.ray.midgard.modules.item.utils.StatRange;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class AttributeUpdater {

    private static final Map<ItemStat, String> STAT_MAPPING = new EnumMap<>(ItemStat.class);

    static {
        STAT_MAPPING.put(ItemStat.MAX_HEALTH, CombatAttributes.MAX_HEALTH);
        STAT_MAPPING.put(ItemStat.MAX_MANA, CombatAttributes.MAX_MANA);
        STAT_MAPPING.put(ItemStat.MAX_STAMINA, CombatAttributes.MAX_STAMINA);
        STAT_MAPPING.put(ItemStat.ATTACK_DAMAGE, CombatAttributes.PHYSICAL_DAMAGE);
        STAT_MAPPING.put(ItemStat.DEFENSE, CombatAttributes.DEFENSE);
        STAT_MAPPING.put(ItemStat.CRITICAL_STRIKE_CHANCE, CombatAttributes.CRITICAL_CHANCE);
        STAT_MAPPING.put(ItemStat.CRITICAL_STRIKE_POWER, CombatAttributes.CRITICAL_DAMAGE);
        STAT_MAPPING.put(ItemStat.DODGE_RATING, CombatAttributes.DODGE_RATING);
        STAT_MAPPING.put(ItemStat.PARRY_RATING, CombatAttributes.PARRY_RATING);
        STAT_MAPPING.put(ItemStat.BLOCK_RATING, CombatAttributes.BLOCK_RATING);
        STAT_MAPPING.put(ItemStat.BLOCK_POWER, CombatAttributes.BLOCK_POWER);
        STAT_MAPPING.put(ItemStat.ARMOR, CombatAttributes.ARMOR);
        STAT_MAPPING.put(ItemStat.ARMOR_TOUGHNESS, CombatAttributes.ARMOR_TOUGHNESS);
        STAT_MAPPING.put(ItemStat.MOVEMENT_SPEED, CombatAttributes.SPEED);
        STAT_MAPPING.put(ItemStat.COOLDOWN_REDUCTION, CombatAttributes.COOLDOWN_REDUCTION);
        STAT_MAPPING.put(ItemStat.LIFESTEAL, CombatAttributes.LIFE_STEAL);
        
        // Regeneration
        STAT_MAPPING.put(ItemStat.HEALTH_REGENERATION, CombatAttributes.HEALTH_REGEN);
        STAT_MAPPING.put(ItemStat.MAX_HEALTH_REGENERATION, CombatAttributes.MAX_HEALTH_REGEN);
        STAT_MAPPING.put(ItemStat.MANA_REGENERATION, CombatAttributes.MANA_REGEN);
        STAT_MAPPING.put(ItemStat.MAX_MANA_REGENERATION, CombatAttributes.MAX_MANA_REGEN);
        STAT_MAPPING.put(ItemStat.STAMINA_REGENERATION, CombatAttributes.STAMINA_REGEN);
        STAT_MAPPING.put(ItemStat.MAX_STAMINA_REGENERATION, CombatAttributes.MAX_STAMINA_REGEN);

        // Other Stats
        STAT_MAPPING.put(ItemStat.MAX_STELLIUM, CombatAttributes.MAX_STELLIUM);
        STAT_MAPPING.put(ItemStat.MAX_ABSORPTION, CombatAttributes.MAX_ABSORPTION);
        STAT_MAPPING.put(ItemStat.KNOCKBACK_RESISTANCE, CombatAttributes.KNOCKBACK_RESISTANCE);
        STAT_MAPPING.put(ItemStat.MYLUCK, CombatAttributes.LUCK);
        STAT_MAPPING.put(ItemStat.ATTACK_SPEED, CombatAttributes.ATTACK_SPEED);

        // Damage & Defense
        STAT_MAPPING.put(ItemStat.WEAPON_DAMAGE, CombatAttributes.WEAPON_DAMAGE);
        STAT_MAPPING.put(ItemStat.SKILL_DAMAGE, CombatAttributes.SKILL_DAMAGE);
        STAT_MAPPING.put(ItemStat.PROJECTILE_DAMAGE, CombatAttributes.PROJECTILE_DAMAGE);
        STAT_MAPPING.put(ItemStat.MAGIC_DAMAGE, CombatAttributes.MAGIC_DAMAGE);
        STAT_MAPPING.put(ItemStat.PHYSICAL_DAMAGE, CombatAttributes.PHYSICAL_DAMAGE);
        STAT_MAPPING.put(ItemStat.UNDEAD_DAMAGE, CombatAttributes.UNDEAD_DAMAGE);
        
        STAT_MAPPING.put(ItemStat.DAMAGE_REDUCTION, CombatAttributes.DAMAGE_REDUCTION);
        STAT_MAPPING.put(ItemStat.FALL_DAMAGE_REDUCTION, CombatAttributes.FALL_DAMAGE_REDUCTION);
        STAT_MAPPING.put(ItemStat.PROJECTILE_DAMAGE_REDUCTION, CombatAttributes.PROJECTILE_DAMAGE_REDUCTION);
        STAT_MAPPING.put(ItemStat.PHYSICAL_DAMAGE_REDUCTION, CombatAttributes.PHYSICAL_DAMAGE_REDUCTION);
        STAT_MAPPING.put(ItemStat.MAGIC_DAMAGE_REDUCTION, CombatAttributes.MAGIC_DAMAGE_REDUCTION);
        STAT_MAPPING.put(ItemStat.PVE_DAMAGE_REDUCTION, CombatAttributes.PVE_DAMAGE_REDUCTION);
        STAT_MAPPING.put(ItemStat.PVP_DAMAGE_REDUCTION, CombatAttributes.PVP_DAMAGE_REDUCTION);
        STAT_MAPPING.put(ItemStat.FIRE_DAMAGE_REDUCTION, CombatAttributes.FIRE_DEFENSE);
        
        STAT_MAPPING.put(ItemStat.SPELL_VAMPIRISM, CombatAttributes.SPELL_VAMPIRISM);
        STAT_MAPPING.put(ItemStat.BLOCK_COOLDOWN_REDUCTION, CombatAttributes.BLOCK_COOLDOWN_REDUCTION);
        STAT_MAPPING.put(ItemStat.DODGE_COOLDOWN_REDUCTION, CombatAttributes.DODGE_COOLDOWN_REDUCTION);
        STAT_MAPPING.put(ItemStat.PARRY_COOLDOWN_REDUCTION, CombatAttributes.PARRY_COOLDOWN_REDUCTION);
        STAT_MAPPING.put(ItemStat.SKILL_CRITICAL_STRIKE_CHANCE, CombatAttributes.SKILL_CRITICAL_CHANCE);
        STAT_MAPPING.put(ItemStat.SKILL_CRITICAL_STRIKE_POWER, CombatAttributes.SKILL_CRITICAL_DAMAGE);

        // Elemental
        STAT_MAPPING.put(ItemStat.FIRE_DAMAGE, CombatAttributes.FIRE_DAMAGE);
        STAT_MAPPING.put(ItemStat.ICE_DAMAGE, CombatAttributes.ICE_DAMAGE);
        STAT_MAPPING.put(ItemStat.LIGHT_DAMAGE, CombatAttributes.LIGHT_DAMAGE);
        STAT_MAPPING.put(ItemStat.DARKNESS_DAMAGE, CombatAttributes.DARKNESS_DAMAGE);
        STAT_MAPPING.put(ItemStat.DIVINE_DAMAGE, CombatAttributes.DIVINE_DAMAGE);

        // New RPG Stats
        STAT_MAPPING.put(ItemStat.STRENGTH, CombatAttributes.STRENGTH);
        STAT_MAPPING.put(ItemStat.INTELLIGENCE, CombatAttributes.INTELLIGENCE);
        STAT_MAPPING.put(ItemStat.DEXTERITY, CombatAttributes.DEXTERITY);
        STAT_MAPPING.put(ItemStat.ACCURACY, CombatAttributes.ACCURACY);
        STAT_MAPPING.put(ItemStat.CRITICAL_RESISTANCE, CombatAttributes.CRITICAL_RESISTANCE);
        STAT_MAPPING.put(ItemStat.THORNS, CombatAttributes.THORNS);
        STAT_MAPPING.put(ItemStat.MAGIC_RESISTANCE, CombatAttributes.MAGIC_RESISTANCE);
        
        STAT_MAPPING.put(ItemStat.ARMOR_PENETRATION, CombatAttributes.ARMOR_PENETRATION);
        STAT_MAPPING.put(ItemStat.ARMOR_PENETRATION_FLAT, CombatAttributes.ARMOR_PENETRATION_FLAT);
        STAT_MAPPING.put(ItemStat.MAGIC_PENETRATION, CombatAttributes.MAGIC_PENETRATION);
        STAT_MAPPING.put(ItemStat.MAGIC_PENETRATION_FLAT, CombatAttributes.MAGIC_PENETRATION_FLAT);
        
        STAT_MAPPING.put(ItemStat.ICE_DAMAGE_REDUCTION, CombatAttributes.ICE_DEFENSE);
        STAT_MAPPING.put(ItemStat.LIGHT_DAMAGE_REDUCTION, CombatAttributes.LIGHT_DEFENSE);
        STAT_MAPPING.put(ItemStat.DARKNESS_DAMAGE_REDUCTION, CombatAttributes.DARKNESS_DEFENSE);
        STAT_MAPPING.put(ItemStat.DIVINE_DAMAGE_REDUCTION, CombatAttributes.DIVINE_DEFENSE);
    }

    public static void updateAttributes(Player player) {
        updateAttributes(player, -1);
    }

    public static void updateAttributes(Player player, int overrideMainHandSlot) {
        MidgardProfile profile = MidgardCore.getProfileManager().getProfile(player.getUniqueId());
        if (profile == null) return;

        CoreAttributeData attributeData = profile.getData(CoreAttributeData.class);
        if (attributeData == null) return;

        // DEBUG LOG
        // ItemModule.getInstance().getPlugin().getLogger().info("DEBUG: Updating attributes for " + player.getName());

        // 1. Clear existing equipment modifiers
        for (AttributeInstance instance : attributeData.getInstances().values()) {
            instance.removeModifier("Equipment");
        }

        // 2. Calculate new totals
        Map<String, Double> totals = new HashMap<>();

        ItemStack[] armor = player.getInventory().getArmorContents();
        // Armor contents: [Boots, Leggings, Chestplate, Helmet]
        // Indices: 0=Boots, 1=Leggings, 2=Chestplate, 3=Helmet
        
        processItem(armor[0], totals, EquipmentSlot.FEET);
        processItem(armor[1], totals, EquipmentSlot.LEGS);
        processItem(armor[2], totals, EquipmentSlot.CHEST);
        processItem(armor[3], totals, EquipmentSlot.HEAD);
        
        ItemStack mainHand;
        if (overrideMainHandSlot != -1) {
            mainHand = player.getInventory().getItem(overrideMainHandSlot);
        } else {
            mainHand = player.getInventory().getItemInMainHand();
        }

        processItem(mainHand, totals, EquipmentSlot.HAND);
        processItem(player.getInventory().getItemInOffHand(), totals, EquipmentSlot.OFF_HAND);

        // 3. Apply new modifiers
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            String attrId = entry.getKey();
            double value = entry.getValue();

            AttributeInstance instance = attributeData.getInstance(attrId);
            if (instance != null) {
                instance.addModifier(new AttributeModifier("Equipment", value, AttributeOperation.ADD_NUMBER));
                // ItemModule.getInstance().getPlugin().getLogger().info("DEBUG: Applied " + attrId + " -> " + value);
            }
        }
    }

    private static void processItem(ItemStack itemStack, Map<String, Double> totals, EquipmentSlot currentSlot) {
        if (itemStack == null) return;
        
        String id = ItemModule.getInstance().getItemManager().getItemId(itemStack);
        MidgardItem item = (id != null) ? ItemModule.getInstance().getItemManager().getMidgardItem(id) : null;

        if (item != null) {
            // Validate Slot using MidgardItem definition
            String requiredSlotName = item.getEquippableSlot();
            if (requiredSlotName != null && !requiredSlotName.isEmpty() && !requiredSlotName.equalsIgnoreCase("ANY")) {
                 boolean valid = false;
                 if (requiredSlotName.equalsIgnoreCase("HEAD") && currentSlot == EquipmentSlot.HEAD) valid = true;
                 else if (requiredSlotName.equalsIgnoreCase("CHEST") && currentSlot == EquipmentSlot.CHEST) valid = true;
                 else if (requiredSlotName.equalsIgnoreCase("LEGS") && currentSlot == EquipmentSlot.LEGS) valid = true;
                 else if (requiredSlotName.equalsIgnoreCase("FEET") && currentSlot == EquipmentSlot.FEET) valid = true;
                 else if (requiredSlotName.equalsIgnoreCase("OFF_HAND") && currentSlot == EquipmentSlot.OFF_HAND) valid = true;
                 else if (requiredSlotName.equalsIgnoreCase("HAND") && currentSlot == EquipmentSlot.HAND) valid = true;
                 
                 if (!valid) {
                     // ItemModule.getInstance().getPlugin().getLogger().info("DEBUG: Invalid slot for " + id + ": " + currentSlot + " (Required: " + requiredSlotName + ")");
                     return;
                 }
            }
        } else {
            // Fallback validation for items not in registry or without ID
            if (!isValidSlot(itemStack, currentSlot)) {
                // ItemModule.getInstance().getPlugin().getLogger().info("DEBUG: Invalid vanilla slot for " + itemStack.getType() + ": " + currentSlot);
                return;
            }
        }

        Map<ItemStat, Double> itemStats = ItemPDC.getStats(itemStack);
        if (itemStats.isEmpty() && item != null) {
            // Fallback for legacy items
            for (Map.Entry<ItemStat, StatRange> statEntry : item.getStats().entrySet()) {
                itemStats.put(statEntry.getKey(), statEntry.getValue().getMin());
            }
        }
        
        // ItemModule.getInstance().getPlugin().getLogger().info("DEBUG: Processing " + (id != null ? id : itemStack.getType()) + " in " + currentSlot + ". Stats: " + itemStats);

        for (Map.Entry<ItemStat, Double> statEntry : itemStats.entrySet()) {
            ItemStat stat = statEntry.getKey();
            double value = statEntry.getValue();
            
            String attrId = STAT_MAPPING.get(stat);
            if (attrId != null) {
                totals.merge(attrId, value, (a, b) -> a + b);
            }
        }
    }

    private static boolean isValidSlot(ItemStack item, EquipmentSlot slot) {
        String type = item.getType().name();
        if (slot == EquipmentSlot.HEAD) return type.endsWith("_HELMET") || type.endsWith("_SKULL") || type.endsWith("_HEAD");
        if (slot == EquipmentSlot.CHEST) return type.endsWith("_CHESTPLATE") || type.equals("ELYTRA");
        if (slot == EquipmentSlot.LEGS) return type.endsWith("_LEGGINGS");
        if (slot == EquipmentSlot.FEET) return type.endsWith("_BOOTS");
        
        // For hands, allow anything EXCEPT armor
        if (slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND) {
            return !type.endsWith("_HELMET") && !type.endsWith("_CHESTPLATE") && !type.endsWith("_LEGGINGS") && !type.endsWith("_BOOTS");
        }
        return false;
    }
}
