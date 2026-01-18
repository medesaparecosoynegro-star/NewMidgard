package me.ray.midgard.modules.item.listener;

import me.ray.midgard.modules.item.ItemModule;
import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.core.profile.MidgardProfile;
import me.ray.midgard.modules.classes.ClassData;
import me.ray.midgard.modules.item.model.MidgardItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import me.ray.midgard.modules.item.manager.AttributeUpdater;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class EquipListener implements Listener {

    private final ItemModule module;

    public EquipListener(ItemModule module) {
        this.module = module;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        AttributeUpdater.updateAttributes(event.getPlayer());
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        me.ray.midgard.modules.item.task.EquipmentUpdateTask.clearCache(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        // Update attributes immediately using the new slot
        AttributeUpdater.updateAttributes(event.getPlayer(), event.getNewSlot());
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        module.getPlugin().getServer().getScheduler().runTask(module.getPlugin(), () -> {
            AttributeUpdater.updateAttributes(event.getPlayer());
        });
    }
    
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        module.getPlugin().getServer().getScheduler().runTask(module.getPlugin(), () -> {
            AttributeUpdater.updateAttributes(event.getPlayer());
        });
    }

    private boolean checkRequirements(Player player, ItemStack itemStack) {
        String id = module.getItemManager().getItemId(itemStack);
        if (id == null) return true;
        
        MidgardItem item = module.getItemManager().getMidgardItem(id);
        if (item == null) return true;

        int requiredLevel = item.getRequiredLevel();
        if (requiredLevel <= 0) return true;

        MidgardProfile profile = MidgardCore.getProfileManager().getProfile(player.getUniqueId());
        if (profile == null) return true;

        ClassData classData = profile.getData(ClassData.class);
        // If no class data, assume level 1 or allow? Let's assume level 1.
        int playerLevel = (classData != null) ? classData.getLevel() : 1;

        if (playerLevel < requiredLevel) {
            MessageUtils.send(player, MidgardCore.getLanguageManager().getMessage("item.common.level_requirement", "%s", String.valueOf(requiredLevel)));
            return false;
        }
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) return; // Only Shift+Click
        
        // Ensure we are clicking in the player's inventory (bottom part)
        // If top inventory is open (chest), we only care if clicking bottom
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() != InventoryType.PLAYER) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        if (!checkRequirements((Player) event.getWhoClicked(), item)) return;

        NamespacedKey key = new NamespacedKey(module.getPlugin(), "midgard_equippable_slot");
        if (!item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) return;

        String slotName = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        try {
            EquipmentSlot targetSlot = EquipmentSlot.valueOf(slotName.toUpperCase());
            Player player = (Player) event.getWhoClicked();
            PlayerInventory inv = player.getInventory();
            
            int targetSlotIndex = -1;
            switch (targetSlot) {
                case HEAD: targetSlotIndex = 39; break;
                case CHEST: targetSlotIndex = 38; break;
                case LEGS: targetSlotIndex = 37; break;
                case FEET: targetSlotIndex = 36; break;
                case OFF_HAND: targetSlotIndex = 40; break;
                default: return; // HAND or invalid
            }

            // Check if item is already in that slot (clicking the armor slot itself)
            if (event.getSlot() == targetSlotIndex) return;

            ItemStack currentItemInSlot = inv.getItem(targetSlotIndex);
            if (currentItemInSlot != null && currentItemInSlot.getType() != Material.AIR) {
                // Slot occupied, vanilla behavior usually swaps or does nothing.
                // For custom items, we might want to swap if possible, but Shift-Click usually just moves to first available.
                // If we force it, we must handle the swap.
                return; 
            }

            // Move item to slot
            event.setCancelled(true);
            
            // Handle amount
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
                ItemStack toEquip = item.clone();
                toEquip.setAmount(1);
                inv.setItem(targetSlotIndex, toEquip);
            } else {
                inv.setItem(targetSlotIndex, item);
                event.setCurrentItem(null); // Remove from source
            }
            
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f);

            // Update attributes
            module.getPlugin().getServer().getScheduler().runTask(module.getPlugin(), () -> {
                AttributeUpdater.updateAttributes(player);
            });

        } catch (IllegalArgumentException e) {
            // Ignore
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || item.getType() == Material.AIR) return;
        if (!checkRequirements(player, item)) {
            event.setCancelled(true);
            return;
        }

        if (!item.hasItemMeta()) return;

        NamespacedKey key = new NamespacedKey(module.getPlugin(), "midgard_equippable_slot");
        if (!item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            // Debug: Item does not have the key
            // player.sendMessage("Debug: Item does not have midgard_equippable_slot key.");
            return;
        }

        String slotName = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        // player.sendMessage("Debug: Found slot key: " + slotName);

        try {
            EquipmentSlot targetSlot = EquipmentSlot.valueOf(slotName.toUpperCase());
            
            // Ignore if target is HAND (Main Hand) as we are already holding it there (mostly)
            if (targetSlot == EquipmentSlot.HAND && event.getHand() == EquipmentSlot.HAND) return;
            if (targetSlot == EquipmentSlot.OFF_HAND && event.getHand() == EquipmentSlot.OFF_HAND) return;

            event.setCancelled(true); // Cancel default interaction
            
            PlayerInventory inv = player.getInventory();
            ItemStack currentItemInSlot = inv.getItem(targetSlot);
            
            // Clone item to set in slot (to be safe)
            ItemStack toEquip = item.clone();
            toEquip.setAmount(1); // Usually equip 1
            
            // Handle stack reduction if amount > 1
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
                // If we have a current item in slot, we can't easily swap if we are holding a stack.
                // Usually you can't equip from a stack if the slot is occupied.
                if (currentItemInSlot != null && currentItemInSlot.getType() != Material.AIR) {
                    MessageUtils.send(player, MidgardCore.getLanguageManager().getMessage("item.equip.stack_occupied"));
                    return; 
                }
                inv.setItem(targetSlot, toEquip);
            } else {
                // Simple swap
                inv.setItem(targetSlot, toEquip);
                if (event.getHand() == EquipmentSlot.HAND) {
                    inv.setItemInMainHand(currentItemInSlot);
                } else {
                    inv.setItemInOffHand(currentItemInSlot);
                }
            }
            
            player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1.0f, 1.0f);
            
            // Update attributes
            module.getPlugin().getServer().getScheduler().runTask(module.getPlugin(), () -> {
                AttributeUpdater.updateAttributes(player);
            });
            
        } catch (IllegalArgumentException e) {
            MessageUtils.send(player, MidgardCore.getLanguageManager().getMessage("item.equip.invalid_slot", "%s", slotName));
        }
    }
}
