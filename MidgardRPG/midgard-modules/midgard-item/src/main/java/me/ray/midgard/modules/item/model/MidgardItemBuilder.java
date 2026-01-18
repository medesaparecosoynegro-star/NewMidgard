package me.ray.midgard.modules.item.model;

import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.modules.item.ItemModule;
import me.ray.midgard.modules.item.socket.SocketData;
import me.ray.midgard.modules.item.utils.ItemPDC;
import me.ray.midgard.modules.item.utils.LoreFormatter;
import me.ray.midgard.modules.item.utils.StatRange;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Keyed;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;

/**
 * Construtor de ItemStack para itens do MidgardRPG.
 * <p>
 * Responsável por converter um objeto {@link MidgardItem} em um {@link ItemStack} do Bukkit,
 * aplicando todas as propriedades visuais e metadados (Lore, Nome, ModelData, etc.).
 */
public class MidgardItemBuilder {

    private final MidgardItem item;

    /**
     * Construtor do MidgardItemBuilder.
     *
     * @param item O item do MidgardRPG a ser construído.
     */
    public MidgardItemBuilder(MidgardItem item) {
        this.item = Objects.requireNonNull(item, "MidgardItem não pode ser nulo");
    }

    /**
     * Constrói o ItemStack final.
     *
     * @return O ItemStack configurado.
     */
    public ItemStack build() {
        if (item.getMaterial() == null) {
            ItemModule.getInstance().getPlugin().getLogger().warning("Material do item é nulo para o item: " + item.getId());
            return new ItemStack(Material.AIR);
        }
        ItemStack itemStack = new ItemStack(item.getMaterial());
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null) {
            // Roll stats
            Map<ItemStat, Double> rolledStats = new HashMap<>();
            for (Map.Entry<ItemStat, StatRange> entry : item.getStats().entrySet()) {
                double val = entry.getValue().getRandom();
                if (val != 0) {
                    rolledStats.put(entry.getKey(), val);
                    ItemPDC.setStat(meta, entry.getKey(), val);
                }
            }

            if (item.getDisplayName() != null) {
                meta.displayName(MessageUtils.parse(item.getDisplayName()));
            }
            
            // Pass rolled stats to LoreFormatter to display exact values instead of ranges
            List<Component> lore = LoreFormatter.formatLore(item, null, rolledStats);
            meta.lore(lore);

            if (item.getCustomModelData() != 0) {
                setCustomModelData(meta, item.getCustomModelData());
            }
            
            if (item.getItemModel() != null && !item.getItemModel().isEmpty()) {
                try {
                    NamespacedKey key = NamespacedKey.fromString(item.getItemModel());
                    if (key != null) {
                        java.lang.reflect.Method setItemModel = meta.getClass().getMethod("setItemModel", NamespacedKey.class);
                        setItemModel.setAccessible(true);
                        setItemModel.invoke(meta, key);
                    }
                } catch (NoSuchMethodException e) {
                    // Feature not supported on this server version
                } catch (Exception e) {
                    ItemModule.getInstance().getPlugin().getLogger().log(Level.WARNING, "Falha ao definir modelo do item para " + item.getId(), e);
                }
            }

            if (item.getEquippableSlot() != null && !item.getEquippableSlot().isEmpty()) {
                // Save to PDC for custom listener support
                NamespacedKey slotKey = new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_equippable_slot");
                meta.getPersistentDataContainer().set(slotKey, PersistentDataType.STRING, item.getEquippableSlot());

                try {
                    EquipmentSlot slot = EquipmentSlot.valueOf(item.getEquippableSlot().toUpperCase());
                    try {
                        java.lang.reflect.Method getEquippable = meta.getClass().getMethod("getEquippable");
                        getEquippable.setAccessible(true);
                        Object equippableComponent = getEquippable.invoke(meta);
                        
                        java.lang.reflect.Method setSlot = equippableComponent.getClass().getMethod("setSlot", EquipmentSlot.class);
                        setSlot.setAccessible(true);
                        setSlot.invoke(equippableComponent, slot);
                        
                    } catch (Exception e) {
                        // Component API not available or failed
                    }
                } catch (IllegalArgumentException e) {
                    // Invalid slot name
                }
            }

            if (item.getEquippableModel() != null && !item.getEquippableModel().isEmpty()) {
                try {
                    String modelKey = item.getEquippableModel().toLowerCase();
                    NamespacedKey key = modelKey.contains(":") 
                        ? new NamespacedKey(modelKey.split(":")[0], modelKey.split(":")[1])
                        : NamespacedKey.minecraft(modelKey);

                    try {
                        java.lang.reflect.Method getEquippable = meta.getClass().getMethod("getEquippable");
                        getEquippable.setAccessible(true);
                        Object equippableComponent = getEquippable.invoke(meta);
                        
                        java.lang.reflect.Method setModel = equippableComponent.getClass().getMethod("setModel", NamespacedKey.class);
                        setModel.setAccessible(true);
                        setModel.invoke(equippableComponent, key);

                        // Ensure slot is set if not explicitly defined
                        if (item.getEquippableSlot() == null || item.getEquippableSlot().isEmpty()) {
                            EquipmentSlot defaultSlot = inferSlot(item.getMaterial());
                            if (defaultSlot != null) {
                                java.lang.reflect.Method setSlot = equippableComponent.getClass().getMethod("setSlot", EquipmentSlot.class);
                                setSlot.setAccessible(true);
                                setSlot.invoke(equippableComponent, defaultSlot);
                            }
                        }

                        // Apply changes back to meta
                        try {
                            java.lang.reflect.Method setEquippable = meta.getClass().getMethod("setEquippable", equippableComponent.getClass()); // Or interface
                            setEquippable.setAccessible(true);
                            setEquippable.invoke(meta, equippableComponent);
                        } catch (NoSuchMethodException e) {
                            // Try finding method with interface if implementation class failed
                            for (Class<?> iface : equippableComponent.getClass().getInterfaces()) {
                                try {
                                    java.lang.reflect.Method setEquippable = meta.getClass().getMethod("setEquippable", iface);
                                    setEquippable.setAccessible(true);
                                    setEquippable.invoke(meta, equippableComponent);
                                    break;
                                } catch (NoSuchMethodException ignored) {}
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        ItemModule.getInstance().getPlugin().getLogger().log(Level.WARNING, "Failed to set custom model data: ", e);
                    }
                } catch (Exception e) {
                    ItemModule.getInstance().getPlugin().getLogger().log(Level.WARNING, "Failed to resolve custom model data: ", e);
                }
            }

            if (item.getBaseItemDamage() > 0 && meta instanceof Damageable) {
                ((Damageable) meta).setDamage(item.getBaseItemDamage());
            }
            
            if (item.getMaxVanillaDurability() > 0 && meta instanceof Damageable) {
                ((Damageable) meta).setMaxDamage(item.getMaxVanillaDurability());
            }

            if (rolledStats.containsKey(ItemStat.UNBREAKABLE) && rolledStats.get(ItemStat.UNBREAKABLE) > 0) {
                meta.setUnbreakable(true);
            }

            if (item.isHideEnchantments()) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            if (item.isHideTooltip()) meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            if (item.isHideDurabilityBar()) meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            if (item.isHideArmorTrim()) meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
            
            if (item.getEnchantments() != null && !item.getEnchantments().isEmpty()) {
                String[] enchants = item.getEnchantments().split(",");
                for (String ench : enchants) {
                    String[] parts = ench.split(":");
                    if (parts.length >= 2) {
                        try {
                            String keyStr = parts[0].toLowerCase().trim();
                            NamespacedKey key = NamespacedKey.minecraft(keyStr);
                            // Se for namespace customizado, tentar fromString
                            if (keyStr.contains(":")) {
                                key = NamespacedKey.fromString(keyStr);
                            }
                            
                            if (key != null) {
                                Enchantment enchantment = getRegistry(Enchantment.class).get(key);
                                int level = Integer.parseInt(parts[1].trim());
                                if (enchantment != null) {
                                    meta.addEnchant(enchantment, level, true);
                                } else {
                                    ItemModule.getInstance().getPlugin().getLogger().warning("Encantamento não encontrado: " + parts[0] + " para o item " + item.getId());
                                }
                            }
                        } catch (NumberFormatException e) {
                             ItemModule.getInstance().getPlugin().getLogger().warning("Nível de encantamento inválido para o item " + item.getId() + ": " + parts[1]);
                        } catch (Exception e) {
                             ItemModule.getInstance().getPlugin().getLogger().log(Level.WARNING, "Falha ao aplicar encantamento " + ench + " para o item " + item.getId(), e);
                        }
                    }
                }
            }

            applyAttributes(meta, rolledStats);

            if (meta instanceof ArmorMeta && item.getTrimMaterial() != null && !item.getTrimMaterial().isEmpty() && item.getTrimPattern() != null && !item.getTrimPattern().isEmpty()) {
                try {
                    Registry<TrimMaterial> tmRegistry = getRegistry(TrimMaterial.class);
                    Registry<TrimPattern> tpRegistry = getRegistry(TrimPattern.class);
                    
                    if (tmRegistry != null && tpRegistry != null) {
                        TrimMaterial tm = tmRegistry.get(NamespacedKey.minecraft(item.getTrimMaterial().toLowerCase()));
                        TrimPattern tp = tpRegistry.get(NamespacedKey.minecraft(item.getTrimPattern().toLowerCase()));
                        if (tm != null && tp != null) {
                            ((ArmorMeta) meta).setTrim(new ArmorTrim(tm, tp));
                        } else {
                            ItemModule.getInstance().getPlugin().getLogger().warning("Material ou padrão de acabamento inválido para o item " + item.getId());
                        }
                    }
                } catch (NoClassDefFoundError | NoSuchMethodError e) {
                    // Trims not supported
                } catch (Exception e) {
                    ItemModule.getInstance().getPlugin().getLogger().log(Level.WARNING, "Falha ao definir acabamento de armadura para " + item.getId(), e);
                }
            }

            NamespacedKey idKey = new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_id");
            NamespacedKey revKey = new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_revision");
            
            meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, item.getId());
            meta.getPersistentDataContainer().set(revKey, PersistentDataType.INTEGER, item.getRevisionId());

            if (item.getMaxCustomDurability() > 0) {
                NamespacedKey maxDurKey = new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_max_durability");
                NamespacedKey durKey = new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_durability");
                
                meta.getPersistentDataContainer().set(maxDurKey, PersistentDataType.INTEGER, item.getMaxCustomDurability());
                meta.getPersistentDataContainer().set(durKey, PersistentDataType.INTEGER, item.getMaxCustomDurability());
            }
            
            if (item.isLostWhenBroken()) {
                NamespacedKey lostKey = new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_lost_when_broken");
                meta.getPersistentDataContainer().set(lostKey, PersistentDataType.BYTE, (byte) 1);
            }
            
            if (item.getNbtTags() != null && !item.getNbtTags().isEmpty()) {
                NamespacedKey nbtKey = new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_nbt_tags");
                meta.getPersistentDataContainer().set(nbtKey, PersistentDataType.STRING, item.getNbtTags());
            }
            
            if (item.getCustomModelDataStrings() != null && !item.getCustomModelDataStrings().isEmpty()) {
                NamespacedKey cmdStringKey = new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_custom_model_data_strings");
                meta.getPersistentDataContainer().set(cmdStringKey, PersistentDataType.STRING, item.getCustomModelDataStrings());
            }

            if (item.getPermission() != null && !item.getPermission().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_permission"), PersistentDataType.STRING, item.getPermission());
            }
            if (item.getItemParticles() != null && !item.getItemParticles().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_item_particles"), PersistentDataType.STRING, item.getItemParticles());
            }
            if (item.getRequiredClass() != null && !item.getRequiredClass().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_required_class"), PersistentDataType.STRING, item.getRequiredClass());
            }
            
            if (item.isDisableInteraction()) meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_disable_interaction"), PersistentDataType.BYTE, (byte) 1);
            if (item.isDisableCrafting()) meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_disable_crafting"), PersistentDataType.BYTE, (byte) 1);
            if (item.isDisableSmelting()) meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_disable_smelting"), PersistentDataType.BYTE, (byte) 1);
            if (item.isDisableRepairing()) meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_disable_repairing"), PersistentDataType.BYTE, (byte) 1);
            if (item.isDisableEnchanting()) meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_disable_enchanting"), PersistentDataType.BYTE, (byte) 1);
            if (item.isDisableSmithing()) meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_disable_smithing"), PersistentDataType.BYTE, (byte) 1);
            if (item.isDisableItemDropping()) meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_disable_item_dropping"), PersistentDataType.BYTE, (byte) 1);

            if (item.getTier() != null && !item.getTier().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_tier"), PersistentDataType.STRING, item.getTier());
            }
            if (item.getItemSet() != null && !item.getItemSet().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_item_set"), PersistentDataType.STRING, item.getItemSet());
            }
            if (item.getRequiredBiomes() != null && !item.getRequiredBiomes().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_required_biomes"), PersistentDataType.STRING, String.join(",", item.getRequiredBiomes()));
            }
            if (item.isDisableDropOnDeath()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_disable_drop_on_death"), PersistentDataType.BYTE, (byte) 1);
            }
            if (item.getCameraOverlay() != null && !item.getCameraOverlay().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_camera_overlay"), PersistentDataType.STRING, item.getCameraOverlay());
            }
            if (item.isUnstackable()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_unstackable"), PersistentDataType.BYTE, (byte) 1);
            }

            if (item.getCooldownReference() != null && !item.getCooldownReference().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_cooldown_reference"), PersistentDataType.STRING, item.getCooldownReference());
            }
            if (item.getCraftingRecipePermission() != null && !item.getCraftingRecipePermission().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_crafting_recipe_permission"), PersistentDataType.STRING, item.getCraftingRecipePermission());
            }
            if (item.getCustomSounds() != null && !item.getCustomSounds().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_custom_sounds"), PersistentDataType.STRING, String.join(",", item.getCustomSounds()));
            }
            if (item.getPermanentEffects() != null && !item.getPermanentEffects().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_permanent_effects"), PersistentDataType.STRING, String.join(",", item.getPermanentEffects()));
            }
            if (item.getGrantedPermissions() != null && !item.getGrantedPermissions().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_granted_permissions"), PersistentDataType.STRING, String.join(",", item.getGrantedPermissions()));
            }
            if (item.getCommands() != null && !item.getCommands().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_commands"), PersistentDataType.STRING, String.join(";;;", item.getCommands()));
            }
            if (item.getCompatibleTypes() != null && !item.getCompatibleTypes().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_compatible_types"), PersistentDataType.STRING, String.join(",", item.getCompatibleTypes()));
            }
            if (item.getCompatibleIds() != null && !item.getCompatibleIds().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_compatible_ids"), PersistentDataType.STRING, String.join(",", item.getCompatibleIds()));
            }
            if (item.getCompatibleMaterials() != null && !item.getCompatibleMaterials().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_compatible_materials"), PersistentDataType.STRING, String.join(",", item.getCompatibleMaterials()));
            }
            if (item.getRepairReference() != null && !item.getRepairReference().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_repair_reference"), PersistentDataType.STRING, item.getRepairReference());
            }
            if (item.isAmphibian()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_amphibian"), PersistentDataType.BYTE, (byte) 1);
            }
            if (item.getItemAbilities() != null && !item.getItemAbilities().isEmpty()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_item_abilities"), PersistentDataType.STRING, String.join(",", item.getItemAbilities()));
            }
            if (item.getGemSockets() != null && !item.getGemSockets().isEmpty()) {
                // Initialize sockets
                SocketData socketData = new SocketData(item.getGemSockets());
                // We need to save manually because SocketData.save takes ItemStack, but we are working on Meta
                // So we use ItemPDC helper or just replicate logic
                String data = socketData.getSockets().stream()
                        .map(e -> e.getType() + ":" + (e.getGemId() == null ? "null" : e.getGemId()))
                        .collect(java.util.stream.Collectors.joining(";"));
                ItemPDC.setString(meta, "midgard_sockets_data", data);
                
                // Also save definition for reference
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_gem_sockets"), PersistentDataType.STRING, String.join(",", item.getGemSockets()));
            }
            
            if (item.getBrowserIndex() != 0) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_browser_index"), PersistentDataType.INTEGER, item.getBrowserIndex());
            }
            if (item.isDisableAdvancedEnchants()) {
                meta.getPersistentDataContainer().set(new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_disable_advanced_enchants"), PersistentDataType.BYTE, (byte) 1);
            }

            // Stats are already saved in the beginning of the method

            if (item.getMaxStackSize() != 64) {
                try {
                    meta.setMaxStackSize(item.getMaxStackSize());
                } catch (NoSuchMethodError e) {
                    // Method introduced in 1.20.6+
                } catch (Exception e) {
                    ItemModule.getInstance().getPlugin().getLogger().log(Level.WARNING, "Falha ao definir tamanho máximo da pilha para " + item.getId(), e);
                }
            }

            if (item.getVanillaTooltipStyle() != null && !item.getVanillaTooltipStyle().isEmpty()) {
                try {
                    NamespacedKey key = NamespacedKey.fromString(item.getVanillaTooltipStyle());
                    if (key != null) {
                        java.lang.reflect.Method setTooltipStyle = meta.getClass().getMethod("setTooltipStyle", NamespacedKey.class);
                        setTooltipStyle.invoke(meta, key);
                    }
                } catch (NoSuchMethodException | SecurityException e) {
                     // Not supported
                } catch (Exception e) {
                    ItemModule.getInstance().getPlugin().getLogger().log(Level.WARNING, "Falha ao definir estilo de dica para " + item.getId(), e);
                }
            }
            
            if (item.getCustomTooltip() != null && !item.getCustomTooltip().isEmpty()) {
                NamespacedKey tooltipKey = new NamespacedKey(ItemModule.getInstance().getPlugin(), "midgard_custom_tooltip");
                meta.getPersistentDataContainer().set(tooltipKey, PersistentDataType.STRING, item.getCustomTooltip());
            }

            itemStack.setItemMeta(meta);
        }
        
        return itemStack;
    }

    private void applyAttributes(ItemMeta meta, Map<ItemStat, Double> stats) {

        if (stats.containsKey(ItemStat.MAX_HEALTH)) addAttribute(meta, Attribute.MAX_HEALTH, stats.get(ItemStat.MAX_HEALTH));
        if (stats.containsKey(ItemStat.MOVEMENT_SPEED)) addAttribute(meta, Attribute.MOVEMENT_SPEED, stats.get(ItemStat.MOVEMENT_SPEED));
        if (stats.containsKey(ItemStat.ATTACK_DAMAGE)) addAttribute(meta, Attribute.ATTACK_DAMAGE, stats.get(ItemStat.ATTACK_DAMAGE));
        if (stats.containsKey(ItemStat.ATTACK_SPEED)) addAttribute(meta, Attribute.ATTACK_SPEED, stats.get(ItemStat.ATTACK_SPEED));
        if (stats.containsKey(ItemStat.ARMOR)) addAttribute(meta, Attribute.ARMOR, stats.get(ItemStat.ARMOR));
        if (stats.containsKey(ItemStat.ARMOR_TOUGHNESS)) addAttribute(meta, Attribute.ARMOR_TOUGHNESS, stats.get(ItemStat.ARMOR_TOUGHNESS));
        if (stats.containsKey(ItemStat.KNOCKBACK_RESISTANCE)) addAttribute(meta, Attribute.KNOCKBACK_RESISTANCE, stats.get(ItemStat.KNOCKBACK_RESISTANCE));
        if (stats.containsKey(ItemStat.MYLUCK)) addAttribute(meta, Attribute.LUCK, stats.get(ItemStat.MYLUCK));
        
        if (stats.containsKey(ItemStat.MAX_ABSORPTION)) addAttribute(meta, Attribute.MAX_ABSORPTION, stats.get(ItemStat.MAX_ABSORPTION));
        if (stats.containsKey(ItemStat.FALL_DAMAGE_MULTIPLIER)) addAttribute(meta, Attribute.FALL_DAMAGE_MULTIPLIER, stats.get(ItemStat.FALL_DAMAGE_MULTIPLIER));
        if (stats.containsKey(ItemStat.GRAVITY)) addAttribute(meta, Attribute.GRAVITY, stats.get(ItemStat.GRAVITY));
        if (stats.containsKey(ItemStat.JUMP_STRENGTH)) addAttribute(meta, Attribute.JUMP_STRENGTH, stats.get(ItemStat.JUMP_STRENGTH));
        if (stats.containsKey(ItemStat.SAFE_FALL_DISTANCE)) addAttribute(meta, Attribute.SAFE_FALL_DISTANCE, stats.get(ItemStat.SAFE_FALL_DISTANCE));
        if (stats.containsKey(ItemStat.SCALE)) addAttribute(meta, Attribute.SCALE, stats.get(ItemStat.SCALE));
        if (stats.containsKey(ItemStat.STEP_HEIGHT)) addAttribute(meta, Attribute.STEP_HEIGHT, stats.get(ItemStat.STEP_HEIGHT));
        if (stats.containsKey(ItemStat.BURNING_TIME)) addAttribute(meta, Attribute.BURNING_TIME, stats.get(ItemStat.BURNING_TIME));
        if (stats.containsKey(ItemStat.EXPLOSION_KNOCKBACK_RESISTANCE)) addAttribute(meta, Attribute.EXPLOSION_KNOCKBACK_RESISTANCE, stats.get(ItemStat.EXPLOSION_KNOCKBACK_RESISTANCE));
        if (stats.containsKey(ItemStat.MINING_EFFICIENCY)) addAttribute(meta, Attribute.MINING_EFFICIENCY, stats.get(ItemStat.MINING_EFFICIENCY));
        if (stats.containsKey(ItemStat.BONUS_OXYGEN)) addAttribute(meta, Attribute.OXYGEN_BONUS, stats.get(ItemStat.BONUS_OXYGEN));
        if (stats.containsKey(ItemStat.SNEAKING_SPEED)) addAttribute(meta, Attribute.SNEAKING_SPEED, stats.get(ItemStat.SNEAKING_SPEED));
        if (stats.containsKey(ItemStat.SUBMERGED_MINING_SPEED)) addAttribute(meta, Attribute.SUBMERGED_MINING_SPEED, stats.get(ItemStat.SUBMERGED_MINING_SPEED));
        if (stats.containsKey(ItemStat.SWEEPING_DAMAGE_RATIO)) addAttribute(meta, Attribute.SWEEPING_DAMAGE_RATIO, stats.get(ItemStat.SWEEPING_DAMAGE_RATIO));
        if (stats.containsKey(ItemStat.WATER_MOVEMENT_EFFICIENCY)) addAttribute(meta, Attribute.WATER_MOVEMENT_EFFICIENCY, stats.get(ItemStat.WATER_MOVEMENT_EFFICIENCY));
        if (stats.containsKey(ItemStat.BLOCK_INTERACTION_RANGE)) addAttribute(meta, Attribute.BLOCK_INTERACTION_RANGE, stats.get(ItemStat.BLOCK_INTERACTION_RANGE));
        if (stats.containsKey(ItemStat.ENTITY_INTERACTION_RANGE)) addAttribute(meta, Attribute.ENTITY_INTERACTION_RANGE, stats.get(ItemStat.ENTITY_INTERACTION_RANGE));
    }

    private void addAttribute(ItemMeta meta, Attribute attribute, double amount) {
        if (amount == 0) return;
        EquipmentSlot slot = null;
        if (item.getEquippableSlot() != null && !item.getEquippableSlot().isEmpty()) {
            try {
                slot = EquipmentSlot.valueOf(item.getEquippableSlot().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid slot name from config
            }
        }
        
        org.bukkit.inventory.EquipmentSlotGroup group = org.bukkit.inventory.EquipmentSlotGroup.ANY;
        if (slot != null) {
            switch (slot) {
                case HAND -> group = org.bukkit.inventory.EquipmentSlotGroup.HAND;
                case OFF_HAND -> group = org.bukkit.inventory.EquipmentSlotGroup.OFFHAND;
                case FEET -> group = org.bukkit.inventory.EquipmentSlotGroup.FEET;
                case LEGS -> group = org.bukkit.inventory.EquipmentSlotGroup.LEGS;
                case CHEST -> group = org.bukkit.inventory.EquipmentSlotGroup.CHEST;
                case HEAD -> group = org.bukkit.inventory.EquipmentSlotGroup.HEAD;
                default -> { /* Default is already ANY */ }
            }
        }
        
        NamespacedKey key = new NamespacedKey(ItemModule.getInstance().getPlugin(), "stat_" + attribute.getKey().getKey().replace('.', '_').toLowerCase());
        meta.addAttributeModifier(attribute, new AttributeModifier(key, amount, AttributeModifier.Operation.ADD_NUMBER, group));
    }

    @SuppressWarnings("deprecation")
    private <T extends Keyed> Registry<T> getRegistry(Class<T> type) {
        return Bukkit.getRegistry(type);
    }

    private EquipmentSlot inferSlot(Material material) {
        String name = material.name();
        if (name.endsWith("_HELMET")) return EquipmentSlot.HEAD;
        if (name.endsWith("_CHESTPLATE") || name.equals("ELYTRA")) return EquipmentSlot.CHEST;
        if (name.endsWith("_LEGGINGS")) return EquipmentSlot.LEGS;
        if (name.endsWith("_BOOTS")) return EquipmentSlot.FEET;
        return null;
    }

    @SuppressWarnings("deprecation")
    private void setCustomModelData(ItemMeta meta, int data) {
        meta.setCustomModelData(data);
    }
}
