package me.ray.midgard.modules.item.gui;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.gui.PaginatedGui;
import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.core.utils.ItemBuilder;
import me.ray.midgard.modules.item.ItemModule;
import me.ray.midgard.modules.item.gui.editors.impl.DoubleEditor;
import me.ray.midgard.modules.item.model.ItemStat;
import me.ray.midgard.modules.item.model.MidgardItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class DamageTypeEditorGui extends PaginatedGui<ItemStat> {

    private final ItemModule module;
    private final MidgardItem item;
    private final ItemEditionGui parent;

    private static final List<ItemStat> DAMAGE_STATS = Arrays.asList(
            ItemStat.ATTACK_DAMAGE,
            ItemStat.WEAPON_DAMAGE,
            ItemStat.PHYSICAL_DAMAGE,
            ItemStat.MAGIC_DAMAGE,
            ItemStat.PROJECTILE_DAMAGE,
            ItemStat.SKILL_DAMAGE,
            ItemStat.UNDEAD_DAMAGE,
            ItemStat.FIRE_DAMAGE,
            ItemStat.ICE_DAMAGE,
            ItemStat.LIGHT_DAMAGE,
            ItemStat.DARKNESS_DAMAGE,
            ItemStat.DIVINE_DAMAGE
    );

    public DamageTypeEditorGui(Player player, ItemModule module, MidgardItem item, ItemEditionGui parent) {
        super(player, MidgardCore.getLanguageManager().getRawMessage("item.gui.damage_type_editor.title"), DAMAGE_STATS);
        this.module = module;
        this.item = item;
        this.parent = parent;
    }

    @Override
    public ItemStack createItem(ItemStat stat) {
        double value = item.getStat(stat);
        String name = MidgardCore.getLanguageManager().getRawMessage("item.gui.damage_type_editor.stats." + stat.getPath() + ".name");
        if (name == null) name = stat.getName();

        return new ItemBuilder(getIconForStat(stat))
                .name(MessageUtils.parse("<green>" + name))
                .lore(
                        MessageUtils.parse(MidgardCore.getLanguageManager().getRawMessage("item.gui.damage_type_editor.lore.current_value").replace("%value%", String.valueOf(value))),
                        MessageUtils.parse(""),
                        MessageUtils.parse(MidgardCore.getLanguageManager().getRawMessage("item.gui.damage_type_editor.lore.click_edit")),
                        MessageUtils.parse(MidgardCore.getLanguageManager().getRawMessage("item.gui.damage_type_editor.lore.right_click_reset"))
                )
                .build();
    }

    private Material getIconForStat(ItemStat stat) {
        switch (stat) {
            case ATTACK_DAMAGE: return Material.IRON_SWORD;
            case WEAPON_DAMAGE: return Material.DIAMOND_SWORD;
            case PHYSICAL_DAMAGE: return Material.IRON_AXE;
            case MAGIC_DAMAGE: return Material.BLAZE_ROD;
            case PROJECTILE_DAMAGE: return Material.ARROW;
            case SKILL_DAMAGE: return Material.EXPERIENCE_BOTTLE;
            case UNDEAD_DAMAGE: return Material.ROTTEN_FLESH;
            case FIRE_DAMAGE: return Material.FLINT_AND_STEEL;
            case ICE_DAMAGE: return Material.SNOWBALL;
            case LIGHT_DAMAGE: return Material.GLOWSTONE_DUST;
            case DARKNESS_DAMAGE: return Material.COAL;
            case DIVINE_DAMAGE: return Material.NETHER_STAR;
            default: return Material.PAPER;
        }
    }

    @Override
    public void addMenuBorder() {
        super.addMenuBorder();
        // Back button at 49
        inventory.setItem(49, new ItemBuilder(Material.BARRIER)
                .name(MidgardCore.getLanguageManager().getMessage("item.common.back"))
                .build());
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        if (slot == 49) {
            parent.open();
            return;
        }

        // Handle item clicks based on PaginatedGui layout (Rows 1-3, Cols 1-7)
        int row = slot / 9;
        int col = slot % 9;

        if (row >= 1 && row <= 3 && col >= 1 && col <= 7) {
            int relativeIndex = (row - 1) * 7 + (col - 1);
            int index = relativeIndex + (page * maxItemsPerPage);

            if (index < items.size()) {
                ItemStat stat = items.get(index);
                
                if (event.getClick().isRightClick()) {
                    item.setStat(stat, 0.0);
                    item.save();
                    player.sendMessage(MessageUtils.parse(MidgardCore.getLanguageManager().getRawMessage("item.gui.damage_type_editor.messages.reset").replace("%stat%", stat.getName())));
                    new DamageTypeEditorGui(player, module, item, parent).open();
                } else {
                    new DoubleEditor(player, (val) -> {
                        item.setStat(stat, val);
                        item.save();
                        player.sendMessage(MessageUtils.parse(MidgardCore.getLanguageManager().getRawMessage("item.gui.damage_type_editor.messages.updated").replace("%stat%", stat.getName())));
                        new DamageTypeEditorGui(player, module, item, parent).open();
                    }, stat.getName()).open();
                }
                return;
            }
        }
        
        super.onClick(event); // Handles pagination clicks
    }
}
