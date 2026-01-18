package me.ray.midgard.modules.item.gui;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.gui.BaseGui;
import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.core.utils.ItemBuilder;
import me.ray.midgard.modules.item.ItemModule;
import me.ray.midgard.modules.item.listener.ChatInputListener;
import me.ray.midgard.modules.item.model.MidgardItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class EquippableModelSelectionGui extends BaseGui {

    private final MidgardItem item;
    private final ItemEditionGui parent;

    public EquippableModelSelectionGui(Player player, ItemModule module, MidgardItem item, ItemEditionGui parent) {
        super(player, 3, "Equippable Model");
        this.item = item;
        this.parent = parent;
    }

    @Override
    public void initializeItems() {
        // Background
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(MessageUtils.parse(" ")).build();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        // 10: Iron
        inventory.setItem(10, new ItemBuilder(Material.IRON_INGOT)
                .name(MessageUtils.parse("&fIron"))
                .lore(MessageUtils.parse("&7Click to set model to Iron"))
                .build());

        // 11: Gold
        inventory.setItem(11, new ItemBuilder(Material.GOLD_INGOT)
                .name(MessageUtils.parse("&6Gold"))
                .lore(MessageUtils.parse("&7Click to set model to Gold"))
                .build());

        // 12: Diamond
        inventory.setItem(12, new ItemBuilder(Material.DIAMOND)
                .name(MessageUtils.parse("&bDiamond"))
                .lore(MessageUtils.parse("&7Click to set model to Diamond"))
                .build());

        // 13: Netherite
        inventory.setItem(13, new ItemBuilder(Material.NETHERITE_INGOT)
                .name(MessageUtils.parse("&8Netherite"))
                .lore(MessageUtils.parse("&7Click to set model to Netherite"))
                .build());

        // 14: Leather (Default/None)
        inventory.setItem(14, new ItemBuilder(Material.LEATHER)
                .name(MessageUtils.parse("&cLeather (Default)"))
                .lore(MessageUtils.parse("&7Click to set model to Leather"))
                .build());

        // 16: Custom Texture ID
        inventory.setItem(16, new ItemBuilder(Material.NAME_TAG)
                .name(MessageUtils.parse("&eCustom Texture ID"))
                .lore(MessageUtils.parse("&7Click to enter a custom texture ID"))
                .build());

        // Back button
        inventory.setItem(18, new ItemBuilder(Material.ARROW).name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.back")).build());
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 10) {
            setModel("iron");
        } else if (slot == 11) {
            setModel("gold");
        } else if (slot == 12) {
            setModel("diamond");
        } else if (slot == 13) {
            setModel("netherite");
        } else if (slot == 14) {
            setModel("leather");
        } else if (slot == 16) {
            player.closeInventory();
            player.sendMessage(MessageUtils.parse("&aEnter the custom texture ID in chat:"));
            ChatInputListener.requestInput(player, (text) -> {
                String lower = text.toLowerCase();
                item.setEquippableModel(lower);
                item.save();
                player.sendMessage(MessageUtils.parse("&aEquippable model updated to: " + lower));
                parent.open();
            });
        } else if (slot == 18) {
            parent.open();
        }
    }

    private void setModel(String model) {
        item.setEquippableModel(model);
        item.save();
        player.sendMessage(MessageUtils.parse("&aEquippable model updated to: " + model));
        parent.open();
    }
}
