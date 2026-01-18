package me.ray.midgard.modules.item.gui.editors.impl;

import me.ray.midgard.core.gui.BaseGui;
import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.core.utils.ItemBuilder;
import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.modules.item.ItemModule;
import me.ray.midgard.modules.item.model.MidgardItem;
import me.ray.midgard.modules.item.gui.IngredientSelectionGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class SmithingRecipeEditorGui extends BaseGui {

    private final ItemModule module;
    private final MidgardItem item;
    private final BaseGui parent;
    private final String RECIPE_TYPE = "SMITHING";

    public SmithingRecipeEditorGui(Player player, ItemModule module, MidgardItem item, BaseGui parent) {
        super(player, 6, "key:item.gui.crafting_gui.editor.titles.smithing");
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

        // Slots 23, 24, 25: Separator
        ItemStack separator = new ItemBuilder(Material.IRON_BARS)
                .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.separator"))
                .build();
        inventory.setItem(23, separator);
        inventory.setItem(24, separator);
        inventory.setItem(25, separator);

        // Slot 40: Base Item (Input 1)
        Map<Integer, String> ingredients = item.getCraftingIngredients(RECIPE_TYPE);
        String baseIng = ingredients.get(0);
        ItemStack baseItem = getItemStack(baseIng);
        inventory.setItem(40, baseItem);

        // Slot 41: Addition Item (Input 2)
        String addIng = ingredients.get(1);
        ItemStack addItem = getItemStack(addIng);
        inventory.setItem(41, addItem);
        
        // Slot 42: Template Item (Input 3 - 1.20+)
        // Assuming slot 2 is template if supported, or maybe smithing table logic varies.
        // Standard smithing: Template (0), Base (1), Addition (2) -> Output
        // But older versions: Base (0), Addition (1) -> Output
        // I'll stick to 0 and 1 for now as per previous logic if any.
        // Actually, let's check if there is a slot 2.
        String templateIng = ingredients.get(2);
        if (templateIng != null) {
             ItemStack templateItem = getItemStack(templateIng);
             inventory.setItem(39, templateItem); // Put template before base
        } else {
             // Placeholder for template if we want to support it
             inventory.setItem(39, new ItemBuilder(Material.BARRIER).name(MessageUtils.parse("&7Template (Optional)")).build());
        }
    }

    private ItemStack getItemStack(String ingredient) {
        if (ingredient != null) {
            MidgardItem ingItem = module.getItemManager().getItem(ingredient);
            if (ingItem != null) {
                return ingItem.build();
            } else {
                try {
                    return new ItemStack(Material.valueOf(ingredient));
                } catch (IllegalArgumentException e) {
                    return new ItemBuilder(Material.BARRIER).name(MessageUtils.parse("&cInvalid Item: " + ingredient)).build();
                }
            }
        } else {
            return new ItemBuilder(Material.BARRIER)
                    .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.no_item"))
                    .build();
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

        // Handle inputs
        if (slot == 39) { // Template
             handleInputClick(event, 2);
             return;
        }
        if (slot == 40) { // Base
             handleInputClick(event, 0);
             return;
        }
        if (slot == 41) { // Addition
             handleInputClick(event, 1);
             return;
        }
    }

    private void handleInputClick(InventoryClickEvent event, int slotIndex) {
        if (event.getCursor() != null && event.getCursor().getType() != Material.AIR) {
            ItemStack cursor = event.getCursor();
            String ingredient = module.getItemManager().getItemId(cursor);
            if (ingredient == null) {
                ingredient = cursor.getType().name();
            }
            item.setCraftingIngredient(RECIPE_TYPE, slotIndex, ingredient);
            initializeItems();
        } else if (event.getClick() == ClickType.RIGHT) {
            item.setCraftingIngredient(RECIPE_TYPE, slotIndex, null);
            initializeItems();
        } else {
            new IngredientSelectionGui(player, module, this, selectedIngredient -> {
                item.setCraftingIngredient(RECIPE_TYPE, slotIndex, selectedIngredient);
                open();
            }).open();
        }
    }
}
