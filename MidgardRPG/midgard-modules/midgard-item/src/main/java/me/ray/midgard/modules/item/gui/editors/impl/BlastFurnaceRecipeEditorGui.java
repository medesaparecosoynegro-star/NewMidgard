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

import java.util.Map;

public class BlastFurnaceRecipeEditorGui extends BaseGui {

    private final ItemModule module;
    private final MidgardItem item;
    private final BaseGui parent;
    private final String RECIPE_TYPE = "BLAST_FURNACE";

    public BlastFurnaceRecipeEditorGui(Player player, ItemModule module, MidgardItem item, BaseGui parent) {
        super(player, 6, "key:item.gui.crafting_gui.editor.titles.blast_furnace");
        this.module = module;
        this.item = item;
        this.parent = parent;
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

        // Slot 19: Choose Output Amount
        inventory.setItem(19, new ItemBuilder(Material.LEATHER)
                .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.output_amount.name"))
                .lore(MidgardCore.getLanguageManager().getMessageList("item.gui.crafting_gui.editor.common.output_amount.lore"))
                .amount(item.getCraftingOutputAmount(RECIPE_TYPE))
                .build());

        // Slot 20: Hide from Crafting Book
        boolean hidden = item.isCraftingHiddenFromBook(RECIPE_TYPE);
        inventory.setItem(20, new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.hide_book.name"))
                .lore(MidgardCore.getLanguageManager().getMessageList("item.gui.crafting_gui.editor.common.hide_book.lore"))
                .addLoreLine("")
                .addLoreLine(hidden ? "&aHIDDEN" : "&cSHOWN")
                .build());

        // Slot 21: Experience
        inventory.setItem(21, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.experience.name"))
                .lore(MidgardCore.getLanguageManager().getMessageList("item.gui.crafting_gui.editor.common.experience.lore"))
                .addLoreLine("")
                .addLoreLine("&7Current: &f" + item.getCraftingExperience(RECIPE_TYPE))
                .build());

        // Slot 22: Duration
        inventory.setItem(22, new ItemBuilder(Material.CLOCK)
                .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.duration.name"))
                .lore(MidgardCore.getLanguageManager().getMessageList("item.gui.crafting_gui.editor.common.duration.lore"))
                .addLoreLine("")
                .addLoreLine("&7Current: &f" + item.getCraftingDuration(RECIPE_TYPE))
                .build());

        // Slots 23, 24, 25: Separator
        ItemStack separator = new ItemBuilder(Material.IRON_BARS)
                .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.separator"))
                .build();
        inventory.setItem(23, separator);
        inventory.setItem(24, separator);
        inventory.setItem(25, separator);

        // Slot 40: Input Item
        Map<Integer, String> ingredients = item.getCraftingIngredients(RECIPE_TYPE);
        String ingredient = ingredients.get(0); // Slot 0 for blast furnace input
        ItemStack inputItem;
        if (ingredient != null) {
            MidgardItem ingItem = module.getItemManager().getItem(ingredient);
            if (ingItem != null) {
                inputItem = ingItem.build();
            } else {
                try {
                    inputItem = new ItemStack(Material.valueOf(ingredient));
                } catch (IllegalArgumentException e) {
                    inputItem = new ItemBuilder(Material.BARRIER).name(MessageUtils.parse("&cInvalid Item: " + ingredient)).build();
                }
            }
        } else {
            inputItem = new ItemBuilder(Material.BARRIER)
                    .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.no_item"))
                    .build();
        }
        inventory.setItem(40, inputItem);
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

        if (slot == 19) {
            if (event.getClick() == ClickType.RIGHT) {
                item.setCraftingOutputAmount(RECIPE_TYPE, 1);
                initializeItems();
            } else {
                new IntegerEditor(player, val -> {
                    item.setCraftingOutputAmount(RECIPE_TYPE, val);
                    open();
                }, "Output Amount").open();
            }
            return;
        }

        if (slot == 20) {
            item.setCraftingHiddenFromBook(RECIPE_TYPE, !item.isCraftingHiddenFromBook(RECIPE_TYPE));
            initializeItems();
            return;
        }

        if (slot == 21) {
            if (event.getClick() == ClickType.RIGHT) {
                item.setCraftingExperience(RECIPE_TYPE, 0.35);
                initializeItems();
            } else {
                new DoubleEditor(player, val -> {
                    item.setCraftingExperience(RECIPE_TYPE, val);
                    open();
                }, "Experience").open();
            }
            return;
        }

        if (slot == 22) {
            if (event.getClick() == ClickType.RIGHT) {
                item.setCraftingDuration(RECIPE_TYPE, 200);
                initializeItems();
            } else {
                new IntegerEditor(player, val -> {
                    item.setCraftingDuration(RECIPE_TYPE, val);
                    open();
                }, "Duration").open();
            }
            return;
        }

        if (slot == 40) {
            if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
                ItemStack cursor = event.getCursor();
                String ingredient = module.getItemManager().getItemId(cursor);
                if (ingredient == null) {
                    ingredient = cursor.getType().name();
                }
                item.setCraftingIngredient(RECIPE_TYPE, 0, ingredient);
                initializeItems();
            } else if (event.getClick() == ClickType.RIGHT) {
                item.setCraftingIngredient(RECIPE_TYPE, 0, null);
                initializeItems();
            } else {
                new IngredientSelectionGui(player, module, this, selectedIngredient -> {
                    item.setCraftingIngredient(RECIPE_TYPE, 0, selectedIngredient);
                    open();
                }).open();
            }
            return;
        }
    }
}
