package me.ray.midgard.modules.item.gui.editors.impl;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.modules.item.ItemModule;
import me.ray.midgard.modules.item.model.ItemStat;
import me.ray.midgard.modules.item.model.MidgardItem;
import me.ray.midgard.modules.item.gui.ItemEditionGui;
import me.ray.midgard.modules.item.gui.editors.StatEditor;
import me.ray.midgard.modules.item.listener.ChatInputListener;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class RpgStatEditor implements StatEditor {

    private final ItemStat stat;

    public RpgStatEditor(ItemStat stat) {
        this.stat = stat;
    }

    @Override
    public void edit(Player player, ItemModule module, MidgardItem item, ItemEditionGui gui, ClickType clickType) {
        int page = gui.getPage();
        if (clickType.isRightClick()) {
            item.setStat(stat, 0.0);
            player.sendMessage(MidgardCore.getLanguageManager().getMessage("item.gui.editors.stat.reset", "%stat%", stat.getName()));
            new ItemEditionGui(player, module, item, page).open();
            return;
        }
        player.closeInventory();
        player.sendMessage(MidgardCore.getLanguageManager().getMessage("item.gui.editors.stat.enter_value", "%stat%", stat.getName()));
        ChatInputListener.requestInput(player, (text) -> {
            try {
                double val = Double.parseDouble(text);
                item.setStat(stat, val);
                player.sendMessage(MidgardCore.getLanguageManager().getMessage("item.gui.editors.stat.updated", "%stat%", stat.getName()));
                new ItemEditionGui(player, module, item, page).open();
            } catch (NumberFormatException e) {
                player.sendMessage(MidgardCore.getLanguageManager().getMessage("item.gui.editors.stat.invalid_format"));
                new ItemEditionGui(player, module, item, page).open();
            }
        });
    }
}
