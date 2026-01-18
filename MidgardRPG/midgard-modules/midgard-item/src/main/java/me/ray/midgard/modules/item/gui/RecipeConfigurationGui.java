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

import me.ray.midgard.modules.item.gui.editors.impl.SmithingRecipeEditorGui;
import me.ray.midgard.modules.item.gui.editors.impl.SmokerRecipeEditorGui;
import me.ray.midgard.modules.item.gui.editors.impl.FurnaceRecipeEditorGui;
import me.ray.midgard.modules.item.gui.editors.impl.ShapedRecipeEditorGui;
import me.ray.midgard.modules.item.gui.editors.impl.MegaShapedRecipeEditorGui;
import me.ray.midgard.modules.item.gui.editors.impl.CampfireRecipeEditorGui;
import me.ray.midgard.modules.item.gui.editors.impl.ShapelessRecipeEditorGui;
import me.ray.midgard.modules.item.gui.editors.impl.BlastFurnaceRecipeEditorGui;
import me.ray.midgard.modules.item.gui.editors.impl.SuperShapedRecipeEditorGui;

public class RecipeConfigurationGui extends BaseGui {

    private final ItemModule module;
    private final MidgardItem item;
    private final BaseGui parent;
    private final String recipeType;

    public RecipeConfigurationGui(Player player, ItemModule module, MidgardItem item, BaseGui parent, String recipeType) {
        super(player, 6, MidgardCore.getLanguageManager().getRawMessage("item.gui.crafting_gui.configuration.title"));
        this.module = module;
        this.item = item;
        this.parent = parent;
        this.recipeType = recipeType;
    }

    @SuppressWarnings("unused")
    private static String formatType(String type) {
        if (type == null) return "";
        String lower = type.toLowerCase().replace("_", " ");
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    @Override
    public void initializeItems() {
        // Clear inventory first
        inventory.clear();

        // Slot 2: Chest - Get Item
        inventory.setItem(2, new ItemBuilder(Material.CHEST)
                .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.get_item.name"))
                .lore(MidgardCore.getLanguageManager().getMessageList("item.gui.crafting_gui.editor.common.get_item.lore"))
                .build());

        // Slot 4: Item Display
        ItemStack displayItem = item.build();
        inventory.setItem(4, displayItem);

        // Slot 6: Barrier - Back
        inventory.setItem(6, new ItemBuilder(Material.BARRIER)
                .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.editor.common.back"))
                .build());

        // Grid Slots
        int[] gridSlots = {
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        // Logic:
        // 1. Check existing recipes (Simulated for now: 1 if enabled, 0 if not)
        // 2. Place existing recipes
        // 3. Place Nether Star at next slot
        // 4. Place Glass Panes at remaining slots

        boolean hasRecipe = item.isCraftingEnabled(recipeType);
        int currentSlotIndex = 0;

        if (hasRecipe) {
            // Place existing recipe icon
            if (currentSlotIndex < gridSlots.length) {
                inventory.setItem(gridSlots[currentSlotIndex], new ItemBuilder(Material.PAPER)
                        .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.configuration.recipe_item.name", Collections.singletonMap("id", "1")))
                        .lore(MidgardCore.getLanguageManager().getMessageList("item.gui.crafting_gui.configuration.recipe_item.lore"))
                        .build());
                currentSlotIndex++;
            }
        }

        // Place Nether Star (Create New)
        if (currentSlotIndex < gridSlots.length) {
            inventory.setItem(gridSlots[currentSlotIndex], new ItemBuilder(Material.NETHER_STAR)
                    .name(MidgardCore.getLanguageManager().getMessage("item.gui.crafting_gui.configuration.new_recipe.name"))
                    .lore(MidgardCore.getLanguageManager().getMessageList("item.gui.crafting_gui.configuration.new_recipe.lore"))
                    .build());
            currentSlotIndex++;
        }

        // Fill remaining with Glass Panes
        ItemStack glassPane = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name(MessageUtils.parse(" "))
                .build();

        for (int i = currentSlotIndex; i < gridSlots.length; i++) {
            inventory.setItem(gridSlots[i], glassPane);
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
            // Give item
            ItemStack result = item.build();
            result.setAmount(item.getCraftingOutputAmount(recipeType));
            player.getInventory().addItem(result);
            player.sendMessage(MessageUtils.parse(MidgardCore.getLanguageManager().getRawMessage("item.common.item_added")));
            return;
        }

        // Check grid slots
        int[] gridSlots = {
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        boolean hasRecipe = item.isCraftingEnabled(recipeType);
        int recipeSlot = hasRecipe ? gridSlots[0] : -1;
        int newRecipeSlot = hasRecipe ? gridSlots[1] : gridSlots[0];

        if (slot == recipeSlot) {
            if ("SMITHING".equalsIgnoreCase(recipeType)) {
                new SmithingRecipeEditorGui(player, module, item, this).open();
            } else if ("SUPER_SHAPED".equalsIgnoreCase(recipeType)) {
                new SuperShapedRecipeEditorGui(player, module, item, this).open();
            } else if ("SMOKER".equalsIgnoreCase(recipeType)) {
                new SmokerRecipeEditorGui(player, module, item, this).open();
            } else if ("FURNACE".equalsIgnoreCase(recipeType)) {
                new FurnaceRecipeEditorGui(player, module, item, this).open();
            } else if ("SHAPED".equalsIgnoreCase(recipeType)) {
                new ShapedRecipeEditorGui(player, module, item, this).open();
            } else if ("MEGA_SHAPED".equalsIgnoreCase(recipeType)) {
                new MegaShapedRecipeEditorGui(player, module, item, this).open();
            } else if ("CAMPFIRE".equalsIgnoreCase(recipeType)) {
                new CampfireRecipeEditorGui(player, module, item, this).open();
            } else if ("SHAPELESS".equalsIgnoreCase(recipeType)) {
                new ShapelessRecipeEditorGui(player, module, item, this).open();
            } else if ("BLAST_FURNACE".equalsIgnoreCase(recipeType)) {
                new BlastFurnaceRecipeEditorGui(player, module, item, this).open();
            } else {
                player.sendMessage(MessageUtils.parse(MidgardCore.getLanguageManager().getRawMessage("item.gui.recipe_configuration.editing_wip")));
            }
        } else if (slot == newRecipeSlot) {
            // Create new recipe
            item.setCraftingEnabled(recipeType, true);
            initializeItems(); // Refresh GUI
            player.sendMessage(MessageUtils.parse(MidgardCore.getLanguageManager().getRawMessage("item.gui.recipe_configuration.created")));
        }
    }
}
