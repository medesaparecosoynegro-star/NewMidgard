package me.ray.midgard.modules.spells.gui;

import me.ray.midgard.core.gui.BaseGui;
import me.ray.midgard.core.gui.GuiUtils;
import me.ray.midgard.core.gui.VisualState;
import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.modules.spells.SpellsModule;
import me.ray.midgard.modules.spells.data.SpellProfile;
import me.ray.midgard.modules.spells.obj.Spell;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Menu principal de magias - Gerenciamento de slots e estilos de conjuração.
 * Modernizado com sistema de ajuda interativo e feedback visual.
 */
public class MainSpellGUI extends BaseGui {

    private final SpellsModule module;
    private final SpellProfile profile;
    private final int[] guiSlots;
    
    private final int infoSlot = 4;
    private final int resetSlot = 45;
    private final int styleSlot = 53;
    private final int helpSlot = 48; // Novo slot de ajuda

    public MainSpellGUI(Player player, SpellsModule module) {
        super(player, 6, module.getMessage("main_gui.title"));
        this.module = module;
        this.profile = module.getSpellManager().getProfile(player);
        this.guiSlots = new int[]{19, 20, 21, 23, 24, 25};
    }
    
    @Override
    public void initializeItems() {
        // Preencher fundo com vidro cinza
        ItemStack filler = GuiUtils.createFiller();
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, filler);
        }
        
        // Renderizar slots de feitiços baseado no modo atual
        if (profile.getCastingStyle() == SpellProfile.CastingStyle.SKILLBAR) {
            renderSkillbarSlots();
        } else {
            renderComboSlots();
        }
        
        // Botão de informação (slot 4)
        ItemStack info = createInfoButton();
        inventory.setItem(infoSlot, info);
        
        // Botão de reset (slot 45)
        ItemStack reset = createResetButton();
        inventory.setItem(resetSlot, reset);
        
        // Botão de ajuda (slot 48)
        inventory.setItem(helpSlot, GuiUtils.createHelpButton());
        
        // Botão de estilo (slot 53)
        ItemStack style = createStyleButton();
        inventory.setItem(styleSlot, style);
    }
    
    /**
     * Renderiza slots no modo Skillbar.
     */
    private void renderSkillbarSlots() {
        for (int i = 0; i < guiSlots.length; i++) {
            int skillBarSlot = i + 1;
            String spellId = profile.getSkillInSlot(skillBarSlot);
            int guiSlot = guiSlots[i];
            
            ItemStack slotItem;
            if (spellId != null) {
                Spell spell = module.getSpellManager().getSpell(spellId);
                if (spell != null) {
                    slotItem = createFilledSkillbarSlot(skillBarSlot, spell);
                } else {
                    slotItem = createInvalidSkillbarSlot(skillBarSlot, spellId);
                }
            } else {
                slotItem = createEmptySkillbarSlot(skillBarSlot);
            }
            
            inventory.setItem(guiSlot, slotItem);
        }
    }
    
    /**
     * Renderiza slots no modo Combo.
     */
    private void renderComboSlots() {
        for (int i = 0; i < guiSlots.length; i++) {
            int comboSlotId = i + 1;
            String sequence = module.getSpellManager().getDefaultCombo(comboSlotId);
            SpellProfile.ComboBinding binding = profile.getComboSlot(comboSlotId);
            String spellId = (binding != null) ? binding.getSpellId() : null;
            int guiSlot = guiSlots[i];
            
            ItemStack slotItem;
            if (spellId != null) {
                Spell spell = module.getSpellManager().getSpell(spellId);
                if (spell != null) {
                    slotItem = createFilledComboSlot(comboSlotId, spell, sequence);
                } else {
                    slotItem = createInvalidComboSlot(comboSlotId, spellId, sequence);
                }
            } else {
                slotItem = createEmptyComboSlot(comboSlotId, sequence);
            }
            
            inventory.setItem(guiSlot, slotItem);
        }
    }
    
    // ======================================
    // MÉTODOS DE CRIAÇÃO DE BOTÕES
    // ======================================
    
    private ItemStack createInfoButton() {
        List<String> lore = module.getMessageList("main_gui.buttons.info.lore");
        return GuiUtils.createStateIndicator(
            VisualState.INFO,
            module.getMessage("main_gui.buttons.info.name"),
            lore.toArray(new String[0])
        );
    }
    
    private ItemStack createResetButton() {
        List<String> lore = module.getMessageList("main_gui.buttons.reset.lore");
        return GuiUtils.createStateIndicator(
            VisualState.ERROR,
            module.getMessage("main_gui.buttons.reset.name"),
            lore.toArray(new String[0])
        );
    }
    
    
    private ItemStack createStyleButton() {
        boolean isSkillbar = (profile.getCastingStyle() == SpellProfile.CastingStyle.SKILLBAR);
        String messageKey = isSkillbar ? "main_gui.buttons.style_skillbar" : "main_gui.buttons.style_combo";
        
        List<String> lore = module.getMessageList(messageKey + ".lore");
        
        return GuiUtils.createStateIndicator(
            isSkillbar ? VisualState.SUCCESS : VisualState.SPECIAL,
            module.getMessage(messageKey + ".name"),
            true, // com glow
            lore.toArray(new String[0])
        );
    }
    
    // ======================================
    // SLOTS SKILLBAR
    // ======================================
    
    private ItemStack createEmptySkillbarSlot(int slot) {
        String name = module.getMessage("main_gui.buttons.slot_empty.name")
            .replace("%slot%", String.valueOf(slot));
        List<String> lore = module.getMessageList("main_gui.buttons.slot_empty.lore");
        
        return GuiUtils.createStateIndicator(
            VisualState.LOCKED,
            name,
            lore.toArray(new String[0])
        );
    }
    
    private ItemStack createFilledSkillbarSlot(int slot, Spell spell) {
        String name = module.getMessage("main_gui.buttons.slot_filled.name")
            .replace("%slot%", String.valueOf(slot))
            .replace("%spell_name%", spell.getDisplayName());
        
        List<String> lore = module.getMessageList("main_gui.buttons.slot_filled.lore");
        lore = lore.stream()
            .map(line -> line
                .replace("%slot%", String.valueOf(slot))
                .replace("%spell_name%", spell.getDisplayName()))
            .toList();
        
        return GuiUtils.createStateIndicator(
            VisualState.AVAILABLE,
            name,
            true, // com glow
            lore.toArray(new String[0])
        );
    }
    
    private ItemStack createInvalidSkillbarSlot(int slot, String spellId) {
        String name = module.getMessage("main_gui.buttons.slot_invalid.name")
            .replace("%slot%", String.valueOf(slot));
        
        List<String> lore = module.getMessageList("main_gui.buttons.slot_invalid.lore");
        lore = lore.stream()
            .map(line -> line
                .replace("%slot%", String.valueOf(slot))
                .replace("%spell_id%", spellId))
            .toList();
        
        return GuiUtils.createStateIndicator(
            VisualState.ERROR,
            name,
            lore.toArray(new String[0])
        );
    }
    
    // ======================================
    // SLOTS COMBO
    // ======================================
    
    private ItemStack createEmptyComboSlot(int slot, String sequence) {
        String name = module.getMessage("main_gui.buttons.combo_empty.name")
            .replace("%slot%", String.valueOf(slot));
        
        List<String> lore = module.getMessageList("main_gui.buttons.combo_empty.lore");
        lore = lore.stream()
            .map(line -> line
                .replace("%slot%", String.valueOf(slot))
                .replace("%sequence%", sequence))
            .toList();
        
        return GuiUtils.createStateIndicator(
            VisualState.LOCKED,
            name,
            lore.toArray(new String[0])
        );
    }
    
    private ItemStack createFilledComboSlot(int slot, Spell spell, String sequence) {
        String name = module.getMessage("main_gui.buttons.combo_filled.name")
            .replace("%slot%", String.valueOf(slot))
            .replace("%spell_name%", spell.getDisplayName());
        
        List<String> lore = module.getMessageList("main_gui.buttons.combo_filled.lore");
        lore = lore.stream()
            .map(line -> line
                .replace("%slot%", String.valueOf(slot))
                .replace("%spell_name%", spell.getDisplayName())
                .replace("%sequence%", sequence))
            .toList();
        
        return GuiUtils.createStateIndicator(
            VisualState.SPECIAL,
            name,
            true, // com glow
            lore.toArray(new String[0])
        );
    }
    
    private ItemStack createInvalidComboSlot(int slot, String spellId, String sequence) {
        String name = module.getMessage("main_gui.buttons.combo_invalid.name")
            .replace("%slot%", String.valueOf(slot));
        
        List<String> lore = module.getMessageList("main_gui.buttons.combo_invalid.lore");
        lore = lore.stream()
            .map(line -> line
                .replace("%slot%", String.valueOf(slot))
                .replace("%spell_id%", spellId)
                .replace("%sequence%", sequence))
            .toList();
        
        return GuiUtils.createStateIndicator(
            VisualState.ERROR,
            name,
            lore.toArray(new String[0])
        );
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getSlot();

        // Alternar Estilo
        if (slot == styleSlot) {
            if (profile.getCastingStyle() == SpellProfile.CastingStyle.SKILLBAR) {
                profile.setCastingStyle(SpellProfile.CastingStyle.COMBO);
                MessageUtils.send(player, module.getMessage("main_gui.messages.mode_changed_combo"));
            } else {
                profile.setCastingStyle(SpellProfile.CastingStyle.SKILLBAR);
                MessageUtils.send(player, module.getMessage("main_gui.messages.mode_changed_skillbar"));
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
            initializeItems();
            return;
        }
        
        // Resetar
        if (slot == resetSlot) {
            for (int i = 1; i <= 6; i++) {
                profile.setSkillBarSlot(i, null);
                String seq = module.getSpellManager().getDefaultCombo(i);
                profile.setComboSlot(i, seq, null);
            }
            MessageUtils.send(player, module.getMessage("main_gui.messages.reset_success"));
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
            initializeItems();
            return;
        }

        // Info
        if (slot == infoSlot) {
            // Não faz nada - apenas item informativo
            return;
        }
        
        // Ajuda
        if (slot == helpSlot) {
            new MainSpellHelpMenu(player, module, this).open();
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1f, 1f);
            return;
        }
        
        // Verificar cliques nos slots de feitiço
        for (int i = 0; i < guiSlots.length; i++) {
            if (slot == guiSlots[i]) {
                int targetSlot = i + 1;
                boolean isSkillBar = (profile.getCastingStyle() == SpellProfile.CastingStyle.SKILLBAR);
                
                new SpellSelectionGUI(player, module, targetSlot, isSkillBar).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                return;
            }
        }
    }
}
