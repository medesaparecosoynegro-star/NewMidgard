package me.ray.midgard.modules.item.gui.editors.impl;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.gui.BaseGui;
import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.core.utils.ItemBuilder;
import me.ray.midgard.modules.item.ItemModule;
import me.ray.midgard.modules.item.model.MidgardItem;
import me.ray.midgard.modules.item.gui.IngredientSelectionGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ShapelessRecipeEditorGui extends BaseGui {

    private final ItemModule module;
    private final MidgardItem item;
    private final BaseGui parent;
    private final String RECIPE_TYPE = "SHAPELESS";
    private final Map<Integer, Integer> slotMap = new HashMap<>();

    public ShapelessRecipeEditorGui(Player player, ItemModule module, MidgardItem item, BaseGui parent) {
        super(player, 6, "key:item.gui.crafting_gui.editor.titles.shapeless");
        this.module = module;
        this.item = item;
        this.parent = parent;
        
        // Map GUI slots to Recipe slots (1-9)
        slotMap.put(30, 1);
        slotMap.put(31, 2);
        slotMap.put(32, 3);
        slotMap.put(39, 4);
        slotMap.put(40, 5);
        slotMap.put(41, 6);
        slotMap.put(48, 7);
        slotMap.put(49, 8);
        slotMap.put(50, 9);
    }

    @Override
    public void initializeItems() {
        inventory.clear();

        // Background filler
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(MessageUtils.parse(" ")).build();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        // Slot 2: Get Item
        inventory.setItem(2, new ItemBuilder(Material.CHEST)
                .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.get_item.name"))
                .lore(MidgardCore.getLanguageManager().getMessageList("item.gui.crafting_gui.editor.common.get_item.lore"))
                .build());

        // Slot 4: Display Item
        inventory.setItem(4, item.build());

        // Slot 6: Back
        inventory.setItem(6, new ItemBuilder(Material.BARRIER)
                .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.back"))
                .build());

        // Slot 10: Choose Output Amount
        inventory.setItem(10, new ItemBuilder(Material.LEATHER)
                .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.output_amount.name"))
                .lore(MidgardCore.getLanguageManager().getMessageList("item.gui.crafting_gui.editor.common.output_amount.lore"))
                .amount(item.getCraftingOutputAmount(RECIPE_TYPE))
                .build());

        // Slot 11: Switch Output Mode
        boolean isShaped = item.isCraftingShaped(RECIPE_TYPE);
        inventory.setItem(11, new ItemBuilder(Material.CRAFTING_TABLE)
                .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.output_mode.name"))
                .lore(MidgardCore.getLanguageManager().getMessageList("item.gui.crafting_gui.editor.common.output_mode.lore"))
                .addLoreLine("")
                .addLoreLine(isShaped ? MidgardCore.getLanguageManager().getRawMessage("item.gui.crafting_gui.editor.common.output_mode.shaped") : MidgardCore.getLanguageManager().getRawMessage("item.gui.crafting_gui.editor.common.output_mode.shapeless"))
                .build());

        // Slot 12: Hide from Crafting Book
        boolean hidden = item.isCraftingHiddenFromBook(RECIPE_TYPE);
        inventory.setItem(12, new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.hide_book.name"))
                .lore(MidgardCore.getLanguageManager().getMessageList("item.gui.crafting_gui.editor.common.hide_book.lore"))
                .addLoreLine("")
                .addLoreLine(hidden ? MidgardCore.getLanguageManager().getRawMessage("item.gui.crafting_gui.editor.common.hide_book.hidden") : MidgardCore.getLanguageManager().getRawMessage("item.gui.crafting_gui.editor.common.hide_book.shown"))
                .build());

        // Separators
        ItemStack separator = new ItemBuilder(Material.IRON_BARS).name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.separator")).build();
        for (int i = 13; i <= 17; i++) {
            inventory.setItem(i, separator);
        }

        // 3x3 Grid
        Map<Integer, String> ingredients = item.getCraftingIngredients(RECIPE_TYPE);
        ItemStack noItem = new ItemBuilder(Material.BARRIER)
                .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.no_item"))
                .build();

        for (Map.Entry<Integer, Integer> entry : slotMap.entrySet()) {
            int guiSlot = entry.getKey();
            int recipeSlot = entry.getValue();
            String ingredient = ingredients.get(recipeSlot);
            
            if (ingredient != null) {
                MidgardItem ingItem = module.getItemManager().getItem(ingredient);
                if (ingItem != null) {
                    inventory.setItem(guiSlot, ingItem.build());
                } else {
                    try {
                        inventory.setItem(guiSlot, new ItemStack(Material.valueOf(ingredient)));
                    } catch (IllegalArgumentException e) {
                        inventory.setItem(guiSlot, new ItemBuilder(Material.BARRIER).name(MessageUtils.parse(MidgardCore.getLanguageManager().getRawMessage("item.gui.crafting_gui.editor.common.invalid_ingredient").replace("%s", ingredient))).build());
                    }
                }
            } else {
                inventory.setItem(guiSlot, noItem);
            }
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 6) {
            parent.open();
            return;
        }
        
        if (slot == 2) {
             ItemStack result = item.build();
             result.setAmount(item.getCraftingOutputAmount(RECIPE_TYPE));
             player.getInventory().addItem(result);
             player.sendMessage(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.item_added"));
             return;
        }

        if (slot == 10) {
            if (event.getClick() == ClickType.RIGHT) {
                item.setCraftingOutputAmount(RECIPE_TYPE, 1);
                initializeItems();
            } else {
                new IntegerEditor(player, val -> {
                    item.setCraftingOutputAmount(RECIPE_TYPE, val);
                    open();
                }, MidgardCore.getLanguageManager().getRawMessage("item.gui.crafting_gui.editor.common.prompts.output_amount")).open();
            }
            return;
        }

        if (slot == 11) {
            item.setCraftingShaped(RECIPE_TYPE, !item.isCraftingShaped(RECIPE_TYPE));
            initializeItems();
            return;
        }

        if (slot == 12) {
            item.setCraftingHiddenFromBook(RECIPE_TYPE, !item.isCraftingHiddenFromBook(RECIPE_TYPE));
            initializeItems();
            return;
        }

        if (slotMap.containsKey(slot)) {
            int recipeSlot = slotMap.get(slot);
            if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                ItemStack cursor = event.getCursor();
                String ingredient = module.getItemManager().getItemId(cursor);
                if (ingredient == null) {
                    ingredient = cursor.getType().name();
                }
                item.setCraftingIngredient(RECIPE_TYPE, recipeSlot, ingredient);
                initializeItems();
            } else if (event.getClick() == ClickType.RIGHT) {
                item.setCraftingIngredient(RECIPE_TYPE, recipeSlot, null);
                initializeItems();
            } else {
                new IngredientSelectionGui(player, module, this, (ingredient) -> {
                    item.setCraftingIngredient(RECIPE_TYPE, recipeSlot, ingredient);
                }).open();
            }
            return;
        }
    }
}
