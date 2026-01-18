package me.ray.midgard.modules.item.gui;

import me.ray.midgard.core.gui.BaseGui;
import me.ray.midgard.core.utils.ItemBuilder;
import me.ray.midgard.modules.item.ItemModule;
import me.ray.midgard.modules.item.model.MidgardItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class DeleteConfirmationGui extends BaseGui {

    private final ItemModule module;
    private final MidgardItem itemToDelete;
    private final ItemBrowserGui parentGui;

    public DeleteConfirmationGui(Player player, ItemModule module, MidgardItem itemToDelete, ItemBrowserGui parentGui) {
        super(player, 3, me.ray.midgard.core.MidgardCore.getLanguageManager().getRawMessage("item.gui.browser.delete-confirmation.title").replace("%s", itemToDelete.getId()));
        this.module = module;
        this.itemToDelete = itemToDelete;
        this.parentGui = parentGui;
    }

    @Override
    public void initializeItems() {
        // Fill background
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(net.kyori.adventure.text.Component.text(" ")).build();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        // Confirm Button (Green)
        inventory.setItem(11, new ItemBuilder(Material.LIME_CONCRETE)
                .name(me.ray.midgard.core.MidgardCore.getLanguageManager().getMessage("item.gui.browser.delete-confirmation.confirm.name"))
                .addLoreLine(me.ray.midgard.core.MidgardCore.getLanguageManager().getMessage("item.gui.browser.delete-confirmation.confirm.lore"))
                .build());

        // Cancel Button (Red)
        inventory.setItem(15, new ItemBuilder(Material.RED_CONCRETE)
                .name(me.ray.midgard.core.MidgardCore.getLanguageManager().getMessage("item.gui.browser.delete-confirmation.cancel.name"))
                .build());
        
        // Show item in middle
        inventory.setItem(13, itemToDelete.build());
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 11) { // Confirm
            // Delete logic
            module.getItemManager().unregisterItem(itemToDelete.getId());
            // Also delete file
            if (itemToDelete.getFile() != null && itemToDelete.getFile().exists()) {
                itemToDelete.getFile().delete();
            }
            
            player.sendMessage(me.ray.midgard.core.MidgardCore.getLanguageManager().getMessage("item.gui.browser.delete-confirmation.success"));
            
            // Refresh parent GUI list
            new ItemBrowserGui(player, module, parentGui.getCategoryId(), module.getItemManager().getItemsByCategory(parentGui.getCategoryId())).open();
            
        } else if (slot == 15) { // Cancel
            parentGui.open();
        }
    }
}
