package me.ray.midgard.core.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;

public class GuiListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory() == null) return;
        
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof BaseGui) {
            BaseGui gui = (BaseGui) holder;
            try {
                gui.onClick(event);
            } catch (Exception e) {
                event.setCancelled(true);
                me.ray.midgard.core.debug.MidgardLogger.error("Erro ao processar clique na GUI: " + gui.getClass().getSimpleName(), e);
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().sendMessage(me.ray.midgard.core.text.MessageUtils.parse("<red>Erro interno na interface."));
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory() == null) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof BaseGui) {
            BaseGui gui = (BaseGui) holder;
            try {
                gui.onDrag(event);
            } catch (Exception e) {
                event.setCancelled(true);
                me.ray.midgard.core.debug.MidgardLogger.error("Erro ao processar arraste na GUI: " + gui.getClass().getSimpleName(), e);
            }
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof BaseGui gui) {
            try {
                gui.onOpen(event);
            } catch (Exception e) {
                me.ray.midgard.core.debug.MidgardLogger.error("Erro ao processar abertura da GUI: " + gui.getClass().getSimpleName(), e);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof BaseGui gui) {
            try {
                gui.onClose(event);
            } catch (Exception e) {
                me.ray.midgard.core.debug.MidgardLogger.error("Erro ao processar fechamento da GUI: " + gui.getClass().getSimpleName(), e);
            }
        }
    }
}
