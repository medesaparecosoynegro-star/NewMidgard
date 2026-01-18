package me.ray.midgard.modules.item.gui;

import me.ray.midgard.core.gui.BaseGui;
import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.core.utils.ItemBuilder;
import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.modules.item.ItemModule;
import me.ray.midgard.modules.item.model.MidgardItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Map;

public class CraftingEditionGui extends BaseGui {

    @SuppressWarnings("unused")
    private final ItemModule module;
    private final MidgardItem item;
    private final ItemEditionGui parent;

    // Grid slots: 0-8 mapped to inventory slots
    private final int[] gridSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private final int outputSlot = 24;
    private final int enabledSlot = 48;
    private final int backSlot = 45;

    public CraftingEditionGui(Player player, ItemModule module, MidgardItem item, ItemEditionGui parent) {
        super(player, 6, MidgardCore.getLanguageManager().getRawMessage("item.gui.crafting_editor.title").replace("%s", item.getId()));
        this.module = module;
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

        // Grid
        Map<Integer, String> ingredients = item.getCraftingIngredients();
        for (int i = 0; i < 9; i++) {
            int slot = gridSlots[i];
            String ingredient = ingredients.get(i);
            if (ingredient != null) {
                try {
                    // Simple material parsing for now
                    Material mat = Material.valueOf(ingredient);
                    inventory.setItem(slot, new ItemStack(mat));
                } catch (IllegalArgumentException e) {
                    inventory.setItem(slot, new ItemBuilder(Material.BARRIER).name(MessageUtils.parse(MidgardCore.getLanguageManager().getRawMessage("item.gui.crafting_editor.invalid_ingredient").replace("%s", ingredient))).build());
                }
            } else {
                inventory.setItem(slot, new ItemBuilder(Material.AIR).build()); // Clear filler
            }
        }

        // Output
        ItemStack output = item.build();
        output.setAmount(item.getCraftingOutputAmount());
        inventory.setItem(outputSlot, output);

        // Controls
        String enabledText = MidgardCore.getLanguageManager().getRawMessage("item.gui.crafting_editor.crafting_enabled")
                .replace("%s", item.isCraftingEnabled() ? 
                        MidgardCore.getLanguageManager().getRawMessage("item.common.yes") : 
                        MidgardCore.getLanguageManager().getRawMessage("item.common.no"));
        
        inventory.setItem(enabledSlot, new ItemBuilder(item.isCraftingEnabled() ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(MessageUtils.parse(enabledText))
                .lore(Collections.singletonList(MidgardCore.getLanguageManager().getMessage("item.common.click_to_toggle")))
                .build());

        inventory.setItem(backSlot, new ItemBuilder(Material.ARROW).name(MidgardCore.getLanguageManager().getMessage("item.common.back")).build());
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == backSlot) {
            parent.open();
            return;
        }

        if (slot == enabledSlot) {
            item.setCraftingEnabled(!item.isCraftingEnabled());
            initializeItems();
            return;
        }

        if (slot == outputSlot) {
            int amount = item.getCraftingOutputAmount();
            if (event.isLeftClick()) amount++;
            if (event.isRightClick()) amount--;
            if (amount < 1) amount = 1;
            if (amount > 64) amount = 64;
            item.setCraftingOutputAmount(amount);
            initializeItems();
            return;
        }

        // Grid interaction
        for (int i = 0; i < 9; i++) {
            if (slot == gridSlots[i]) {
                ItemStack cursor = event.getCursor();
                if (cursor != null && cursor.getType() != Material.AIR) {
                    // Set ingredient
                    item.setCraftingIngredient(i, cursor.getType().name());
                } else {
                    // Clear ingredient
                    item.setCraftingIngredient(i, null);
                }
                initializeItems();
                return;
            }
        }
    }
}
