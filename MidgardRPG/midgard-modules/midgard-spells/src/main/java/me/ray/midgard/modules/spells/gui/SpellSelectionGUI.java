package me.ray.midgard.modules.spells.gui;

import me.ray.midgard.core.gui.PaginatedGui;
import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.core.utils.ItemBuilder;
import me.ray.midgard.modules.spells.SpellsModule;
import me.ray.midgard.modules.spells.data.SpellProfile;
import me.ray.midgard.modules.spells.obj.Spell;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SpellSelectionGUI extends PaginatedGui<Spell> {

    private final SpellsModule module;
    private final SpellProfile profile;
    private final int targetSlot;
    private final boolean isSkillBar;

    public SpellSelectionGUI(Player player, SpellsModule module, int targetSlot, boolean isSkillBar) {
        super(player, module.getMessage("gui.spell_selection.title"), new ArrayList<>(module.getSpellManager().getSpells()));
        this.module = module;
        this.profile = module.getSpellManager().getProfile(player);
        this.targetSlot = targetSlot;
        this.isSkillBar = isSkillBar;
    }

    @Override
    public ItemStack createItem(Spell spell) {
        return new ItemBuilder(Material.ENCHANTED_BOOK)
                .name(MessageUtils.parse("<green>" + spell.getDisplayName()))
                .lore(parseList(spell.getLore()))
                .addLore(" ")
                .addLore(module.getMessage("gui.spell_selection.items.click_to_equip"))
                .build();
    }

    @Override
    public void initializeItems() {
        super.initializeItems();

        // Fill borders to match standard layout
        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).build();
        
        // Full border except content area (10-16, 19-25, 28-34) and functional buttons
        int[] borderSlots = {
                0, 1, 2, 3, 4, 5, 6, 7, 8,
                9, 17,
                18, 26, // Nav slots (super handles them if needed)
                27, 35,
                36, 37, 38, 39, 40, 41, 42, 43, 44
        };

        for (int slot : borderSlots) {
            if (inventory.getItem(slot) == null || inventory.getItem(slot).getType() == Material.AIR) {
                inventory.setItem(slot, filler);
            }
        }
        
        // Bottom row (45-53)
        // 45: Back
        // 49: Close (Super)
        // 50: Unequip
        ItemStack bottomFiller = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(Component.text(" ")).build();
        for (int i = 45; i < 54; i++) {
             if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                 inventory.setItem(i, bottomFiller);
             }
        }

        // Back Button (49)
        inventory.setItem(49, new ItemBuilder(Material.ARROW)
                .name(MessageUtils.parse(module.getMessage("gui.spell_selection.buttons.back.name")))
                .lore(parseList(module.getMessageList("gui.spell_selection.buttons.back.lore")))
                .build());

        // Ensure slot 50 and 45 are cleared/filled
        inventory.setItem(50, bottomFiller);
        inventory.setItem(45, bottomFiller);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        // Super handles cancellation and pagination (18, 26, 49)
        super.onClick(event);

        int slot = event.getRawSlot();

        // Back Button
        if (slot == 49) {
            new MainSpellGUI(player, module).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }

        // Grid Selection logic
        if (isGridSlot(slot)) {
            int pageStartIndex = page * maxItemsPerPage;
            int localIndex = getLocalIndex(slot);
            
            if (localIndex != -1) {
                int finalIndex = pageStartIndex + localIndex;
                if (finalIndex < items.size()) {
                    Spell selected = items.get(finalIndex);
                    handleEquip(selected);
                }
            }
        }
    }

    private void handleEquip(Spell spell) {
        if (isSkillBar) {
            profile.setSkillBarSlot(targetSlot, spell.getId());
            String msg = module.getMessage("gui.spell_selection.messages.equipped_skillbar")
                    .replace("%spell%", spell.getDisplayName())
                    .replace("%slot%", String.valueOf(targetSlot));
            MessageUtils.send(player, msg);
        } else {
            String sequence = module.getSpellManager().getDefaultCombo(targetSlot);
            profile.setComboSlot(targetSlot, sequence, spell.getId());
            String msg = module.getMessage("gui.spell_selection.messages.equipped_combo")
                    .replace("%spell%", spell.getDisplayName())
                    .replace("%slot%", String.valueOf(targetSlot));
            MessageUtils.send(player, msg);
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        new MainSpellGUI(player, module).open();
    }
    
    private List<Component> parseList(List<String> list) {
        if (list == null) return new ArrayList<>();
        List<Component> components = new ArrayList<>();
        for (String s : list) {
            components.add(MessageUtils.parse(s));
        }
        return components;
    }
    
    // Helper to identify content slots standard to PaginatedGui/ItemBrowser
    private boolean isGridSlot(int slot) {
        int[] slots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        };
        for(int s : slots) if(s == slot) return true;
        return false;
    }
    
    private int getLocalIndex(int slot) {
         int[] slots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        };
        for(int i=0; i<slots.length; i++) {
            if (slots[i] == slot) return i;
        }
        return -1;
    }
}
