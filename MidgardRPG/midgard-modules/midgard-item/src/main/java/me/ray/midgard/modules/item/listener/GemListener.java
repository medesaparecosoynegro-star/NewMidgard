package me.ray.midgard.modules.item.listener;

import me.ray.midgard.modules.item.ItemModule;
import me.ray.midgard.modules.item.model.ItemStat;
import me.ray.midgard.modules.item.model.MidgardItem;
import me.ray.midgard.modules.item.socket.SocketData;
import me.ray.midgard.modules.item.utils.ItemPDC;
import me.ray.midgard.modules.item.utils.LoreFormatter;
import me.ray.midgard.modules.item.utils.StatRange;
import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.text.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class GemListener implements Listener {

    @EventHandler
    public void onGemApply(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();
        
        if (cursor.getType() == Material.AIR) return;
        if (current == null || current.getType() == Material.AIR) return;
        
        String gemId = ItemModule.getInstance().getItemManager().getItemId(cursor);
        String targetId = ItemModule.getInstance().getItemManager().getItemId(current);
        
        if (gemId == null || targetId == null) return;
        
        MidgardItem gemItem = ItemModule.getInstance().getItemManager().getMidgardItem(gemId);
        if (gemItem == null || !gemItem.getCategoryId().equalsIgnoreCase("GEM")) return;
        
        // Check socket compatibility
        // Assuming Gem Tier defines the socket color/type
        String gemType = gemItem.getTier(); 
        if (gemType == null || gemType.isEmpty()) gemType = "ANY"; // Default or universal
        
        SocketData socketData = SocketData.fromItem(current);
        if (!socketData.hasEmptySocket(gemType) && !socketData.hasEmptySocket("ANY")) return;
        
        // Apply gem
        if (socketData.applyGem(gemType, gemId)) {
            event.setCancelled(true);
            
            // Consume gem
            if (cursor.getAmount() > 1) {
                cursor.setAmount(cursor.getAmount() - 1);
            } else {
                event.getView().setCursor(null);
            }
            
            // Update target item
            ItemMeta meta = current.getItemMeta();
            
            // Add stats
            for (Map.Entry<ItemStat, StatRange> entry : gemItem.getStats().entrySet()) {
                double value = entry.getValue().getRandom(); // Roll gem stats once
                if (value != 0) {
                    double currentVal = ItemPDC.getStat(meta, entry.getKey());
                    ItemPDC.setStat(meta, entry.getKey(), currentVal + value);
                }
            }
            
            current.setItemMeta(meta);
            socketData.save(current);
            
            // Update Lore
            ItemMeta updatedMeta = current.getItemMeta();
            List<Component> lore = LoreFormatter.formatLore(current);
            updatedMeta.lore(lore);
            current.setItemMeta(updatedMeta);
            
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1.5f);
            MessageUtils.send(player, MidgardCore.getLanguageManager().getMessage("item.gem.applied_success"));
        }
    }
}
