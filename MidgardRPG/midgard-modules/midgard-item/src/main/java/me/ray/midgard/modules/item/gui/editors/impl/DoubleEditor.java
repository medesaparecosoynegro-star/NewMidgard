package me.ray.midgard.modules.item.gui.editors.impl;

import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.modules.item.ItemModule;
import me.ray.midgard.modules.item.model.MidgardItem;
import me.ray.midgard.modules.item.gui.ItemEditionGui;
import me.ray.midgard.modules.item.gui.editors.StatEditor;
import me.ray.midgard.modules.item.listener.ChatInputListener;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DoubleEditor implements StatEditor {

    private final BiConsumer<MidgardItem, Double> setter;
    private final String prompt;

    // Standalone mode fields
    private Player player;
    private Consumer<Double> simpleCallback;

    public DoubleEditor(BiConsumer<MidgardItem, Double> setter, String prompt) {
        this.setter = setter;
        this.prompt = prompt;
    }

    public DoubleEditor(Player player, Consumer<Double> callback, String prompt) {
        this.player = player;
        this.simpleCallback = callback;
        this.prompt = prompt;
        this.setter = null;
    }

    public void open() {
        if (player == null) return;
        player.closeInventory();
        String msg = me.ray.midgard.core.MidgardCore.getLanguageManager().getRawMessage("item.gui.editor.enter-prompt").replace("%s", prompt);
        player.sendMessage(MessageUtils.parse(msg));
        ChatInputListener.requestInput(player, (text) -> {
            try {
                double val = Double.parseDouble(text);
                if (simpleCallback != null) {
                    simpleCallback.accept(val);
                }
            } catch (NumberFormatException e) {
                player.sendMessage(MessageUtils.parse(me.ray.midgard.core.MidgardCore.getLanguageManager().getRawMessage("item.gui.editor.invalid-number")));
            }
        });
    }

    @Override
    public void edit(Player player, ItemModule module, MidgardItem item, ItemEditionGui gui, ClickType clickType) {
        int page = gui.getPage();
        if (clickType.isRightClick()) {
            setter.accept(item, 0.0);
            String msg = me.ray.midgard.core.MidgardCore.getLanguageManager().getRawMessage("item.gui.editor.reset").replace("%s", prompt);
            player.sendMessage(MessageUtils.parse(msg));
            new ItemEditionGui(player, module, item, page).open();
            return;
        }
        player.closeInventory();
        String msg = me.ray.midgard.core.MidgardCore.getLanguageManager().getRawMessage("item.gui.editor.enter-prompt-cancel").replace("%s", prompt);
        player.sendMessage(MessageUtils.parse(msg));
        ChatInputListener.requestInput(player, (text) -> {
            try {
                double val = Double.parseDouble(text);
                setter.accept(item, val);
                String successMsg = me.ray.midgard.core.MidgardCore.getLanguageManager().getRawMessage("item.gui.editor.updated").replace("%s", prompt);
                player.sendMessage(MessageUtils.parse(successMsg));
                new ItemEditionGui(player, module, item, page).open();
            } catch (NumberFormatException e) {
                player.sendMessage(MessageUtils.parse(me.ray.midgard.core.MidgardCore.getLanguageManager().getRawMessage("item.gui.editor.invalid-number")));
                new ItemEditionGui(player, module, item, page).open();
            }
        });
    }
}
