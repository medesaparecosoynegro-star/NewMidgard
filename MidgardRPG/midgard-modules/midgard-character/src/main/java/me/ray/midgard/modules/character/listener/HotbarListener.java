package me.ray.midgard.modules.character.listener;

import java.util.Arrays;

import me.ray.midgard.modules.character.CharacterModule;
import me.ray.midgard.modules.character.gui.CharacterMenu;
import me.ray.midgard.core.text.MessageUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;

import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class HotbarListener implements Listener {

    private final CharacterModule module;
    private static final int SLOT_INDEX = 8; // 9th slot

    public HotbarListener(CharacterModule module) {
        this.module = module;
    }

    private ItemStack getCompass() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Informações do Personagem", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            meta.lore(Arrays.asList(
                Component.text("Veja as informações do seu", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("personagem, atribua pontos de atributo,", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("acesse sua árvore de habilidades e mais", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Pontos de Atributo: ", NamedTextColor.GREEN).append(Component.text("0", NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false),
                Component.text("✦ Pontos de Habilidade: ", NamedTextColor.AQUA).append(Component.text("0", NamedTextColor.WHITE)).decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Clique para Abrir", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false)
            ));
            
            // Set persistence data
            meta.getPersistentDataContainer().set(module.getCompassKey(), PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private boolean isCompass(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(module.getCompassKey(), PersistentDataType.BYTE);
    }

    public void giveCompass(Player player) {
        if (player == null) return;
        try {
            player.getInventory().setItem(SLOT_INDEX, getCompass());
            String message = module.getMessage("hotbar.compass_received");
            if (message != null && !message.isEmpty()) {
                MessageUtils.send(player, message);
            }
        } catch (Exception e) {
            module.getPlugin().getLogger().log(java.util.logging.Level.WARNING, "Erro ao entregar bússola para o jogador: " + player.getName(), e);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try {
            // Delay giving compass to ensure other plugins don't clear it immediately on join
            module.getPlugin().getServer().getScheduler().runTaskLater(module.getPlugin(), () -> {
                giveCompass(event.getPlayer());
            }, 10L);
        } catch (Exception e) {
             module.getPlugin().getLogger().log(java.util.logging.Level.SEVERE, "Erro no evento de entrada do jogador no HotbarListener", e);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        try {
            // Delay for respawn as well
            module.getPlugin().getServer().getScheduler().runTaskLater(module.getPlugin(), () -> {
                giveCompass(event.getPlayer());
            }, 10L);
        } catch (Exception e) {
             module.getPlugin().getLogger().log(java.util.logging.Level.SEVERE, "Erro no evento de respawn do jogador no HotbarListener", e);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        try {
            if (isCompass(event.getOldCursor()) || isCompass(event.getCursor())) {
                event.setCancelled(true);
                return;
            }
            for (int rawSlot : event.getRawSlots()) {
                // Check if we are dragging over an existing compass
                if (event.getView() != null && isCompass(event.getView().getItem(rawSlot))) {
                    event.setCancelled(true);
                    return;
                }
                
                // Check if we are dragging into the locked slot 8 of the player's inventory
                if (event.getView() != null && event.getView().getTopInventory() != null) {
                    // Calculate if the slot is in the bottom inventory (Player Inventory)
                    int topSize = event.getView().getTopInventory().getSize();
                    if (rawSlot >= topSize) {
                        // It is in the bottom inventory
                        int slot = event.getView().convertSlot(rawSlot);
                        if (slot == SLOT_INDEX) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            module.getPlugin().getLogger().log(java.util.logging.Level.SEVERE, "Erro ao processar clique na hotbar (Drag)", e);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            if (event.getClickedInventory() == null) return;
            if (!(event.getWhoClicked() instanceof Player)) return;
            
            // Prevent interaction with slot 8 in player inventory
            if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
                if (event.getSlot() == SLOT_INDEX) {
                    event.setCancelled(true);
                    return;
                }
            }
            
            if (event.getHotbarButton() == SLOT_INDEX) {
                event.setCancelled(true);
                return;
            }

            // Prevent moving the compass if somehow selected
            if (isCompass(event.getCurrentItem())) {
                event.setCancelled(true);
            }
            
            if (isCompass(event.getCursor())) {
                // Drop it from cursor if it stuck there
                event.getView().setCursor(null);
                event.setCancelled(true);
            }
        } catch (Exception e) {
            module.getPlugin().getLogger().log(java.util.logging.Level.SEVERE, "Erro ao processar clique na hotbar", e);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrop(PlayerDropItemEvent event) {
        try {
            if (isCompass(event.getItemDrop().getItemStack())) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            module.getPlugin().getLogger().log(java.util.logging.Level.SEVERE, "Erro ao processar drop de item", e);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        try {
            Player player = event.getPlayer();
            // If mainhand is holding slot 8, cancel swap
            if (player.getInventory().getHeldItemSlot() == SLOT_INDEX) {
                event.setCancelled(true);
            }
            // Double check item content just in case
            if (isCompass(event.getMainHandItem()) || isCompass(event.getOffHandItem())) {
                event.setCancelled(true);
            }
        } catch (Exception e) {
            module.getPlugin().getLogger().log(java.util.logging.Level.SEVERE, "Erro ao processar troca de itens de mão", e);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        try {
            // Prevent drop on death
            event.getDrops().removeIf(this::isCompass);
            // It will be re-given on respawn
        } catch (Exception e) {
            module.getPlugin().getLogger().log(java.util.logging.Level.SEVERE, "Erro ao processar morte de jogador (Hotbar)", e);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        try {
            if (event.getItem() != null && isCompass(event.getItem())) {
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    new CharacterMenu(event.getPlayer()).open();
                    event.setCancelled(true);
                }
            }
        } catch (Exception e) {
             module.getPlugin().getLogger().log(java.util.logging.Level.SEVERE, "Erro ao processar interação com a bússola", e);
             event.getPlayer().sendMessage("§cOcorreu um erro ao abrir o menu.");
        }
    }
}
