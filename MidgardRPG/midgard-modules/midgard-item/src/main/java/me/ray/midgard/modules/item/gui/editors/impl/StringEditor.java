package me.ray.midgard.modules.item.gui.editors.impl;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.modules.item.ItemModule;
import me.ray.midgard.modules.item.model.MidgardItem;
import me.ray.midgard.modules.item.gui.ItemEditionGui;
import me.ray.midgard.modules.item.gui.editors.StatEditor;
import me.ray.midgard.modules.item.listener.ChatInputListener;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.function.BiConsumer;

public class StringEditor implements StatEditor {

    private final BiConsumer<MidgardItem, String> setter;
    private final String prompt;

    public StringEditor(BiConsumer<MidgardItem, String> setter, String prompt) {
        this.setter = setter;
        this.prompt = prompt;
    }

    @Override
    public void edit(Player player, ItemModule module, MidgardItem item, ItemEditionGui gui, ClickType clickType) {
        int page = gui.getPage();
        if (clickType.isRightClick()) {
            setter.accept(item, "");
            player.sendMessage(MidgardCore.getLanguageManager().getMessage("item.gui.editors.string.cleared", "%prompt%", prompt));
            new ItemEditionGui(player, module, item, page).open();
            return;
        }
        player.closeInventory();
        player.sendMessage(MidgardCore.getLanguageManager().getMessage("item.gui.editors.string.enter_value", "%prompt%", prompt));
        ChatInputListener.requestInput(player, (text) -> {
            setter.accept(item, text);
            player.sendMessage(MidgardCore.getLanguageManager().getMessage("item.gui.editors.string.updated", "%prompt%", prompt));
            new ItemEditionGui(player, module, item, page).open();
        });
    }
}
