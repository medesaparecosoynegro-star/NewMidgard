package me.ray.midgard.modules.classes.gui;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.attribute.Attribute;
import me.ray.midgard.core.attribute.AttributeRegistry;
import me.ray.midgard.core.gui.BaseGui;
import me.ray.midgard.core.profile.MidgardProfile;
import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.core.utils.ItemBuilder;
import me.ray.midgard.modules.classes.ClassData;
import me.ray.midgard.modules.classes.ClassesModule;
import me.ray.midgard.modules.classes.RPGClass;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import me.ray.midgard.core.attribute.CoreAttributeData; // Added import
import java.util.List;
import java.util.stream.Collectors;

public class AttributeDistributionGui extends BaseGui {

    private final ClassesModule module;
    private final List<String> attributes = Arrays.asList("strength", "dexterity", "intelligence", "vitality", "defense");

    public AttributeDistributionGui(Player player, ClassesModule module) {
        super(player, 4, "<gradient:#5e4fa2:#f79459><bold>✦ Distribuição de Atributos ✦</bold></gradient>");
        this.module = module;
    }

    @Override
    public void initializeItems() {
        try {
            MidgardProfile profile = MidgardCore.getProfileManager().getProfile(player);
            if (profile == null) return;
            
            ClassData data = profile.getData(ClassData.class);
            if (data == null) return;
            
            CoreAttributeData coreData = profile.getData(CoreAttributeData.class); // Added CoreAttributeData retrieval
    
            String pointsAvailable = "Pontos Disponíveis";
            String distDesc = "Distribua seus pontos para ficar mais forte!";
            String pointsSpent = "Pontos Gastos";
            String clickToAdd = "Clique para adicionar 1 ponto";
            String cost = "Custo: 1 ponto de atributo";
            
            // Info Item moved to slot 4 (top center)
            inventory.setItem(4, new ItemBuilder(Material.BOOK)
                    .name(MessageUtils.parse("<yellow><bold>✦ " + pointsAvailable + ": <white>" + data.getAttributePoints()))
                    .lore(MessageUtils.parse("<gray>" + distDesc))
                    .build());
    
            // Centered slots for 4 rows (9 cols). Row 2 (index 1) and 3 (index 2).
            // Let's put them in middle row (Row 2, slots 19-25)
            // 4 rows = 0-35. 
            // Center row is index 1 or 2.
            // Let's use slots: 11, 12, 13, 14, 15 (Row 2 centered) 
            // Wait, 4 rows indices:
            // 0-8
            // 9-17
            // 18-26
            // 27-35
            // Previous was 3 rows (indices 0-26). Slots 11-15 were middle.
            // If I increase to 4 rows, I can keep them at 20-24 (Row 3) or 11-15 (Row 2).
            // Let's keep 11-15 if I have 5 items.
            // But wait, the loop uses `slots` array.
            // Let's use 11, 12, 13, 14, 15 (Row 2) and maybe 20,21,22,23,24 (Row 3) if more attributes.
            // But `attributes` list has 5 items.
            // I'll space them out nicely.
            // 11, 13, 15 is 3 items. 5 items fit 11-15 tightly.
            // Let's use 20, 21, 22, 23, 24 for better vertical centering in a 4-row GUI?
            // Center of 4 rows is between row 2 and 3.
            // Let's stick to 3 rows (27 slots) as it was fine, just visuals needed upgrade.
            // If I change rows to 4, I must update the slots array logic or keep it.
            // I'll stick to 3 rows but nicer background.
            
            int[] slots = {11, 12, 13, 14, 15};
            
            for (int i = 0; i < attributes.size(); i++) {
                if (i >= slots.length) break;
                
                String attrId = attributes.get(i);
                Attribute attr = AttributeRegistry.getInstance().getAttribute(attrId);
                if (attr == null) continue;
                
                int currentPoints = data.getSpentPoints(attrId);
                
                double currentValue = 0;
                if (coreData != null && coreData.getInstance(attr) != null) {
                    currentValue = coreData.getInstance(attr).getValue();
                }

                List<String> loreLines = Arrays.asList(
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━━",
                        "<gray>" + pointsSpent + ": <white>" + currentPoints,
                        "",
                        "<green>➜ " + clickToAdd,
                        "<gray>" + cost,
                        "",
                        "<yellow>Valor Atual: <white>" + currentValue,
                        "<dark_gray>━━━━━━━━━━━━━━━━━━━━━"
                );
    
                inventory.setItem(slots[i], new ItemBuilder(Material.PAPER) 
                        .name(MessageUtils.parse("<gradient:#5e4fa2:#f79459><bold>" + attr.getName() + "</bold></gradient>"))
                        .lore(loreLines.stream().map(MessageUtils::parse).collect(Collectors.toList()))
                        .build());
            }
            
            addMenuBorder();
        } catch (Exception e) {
             ClassesModule.getInstance().getPlugin().getLogger().log(java.util.logging.Level.SEVERE, "Erro ao inicializar itens da GUI de atributos", e);
        }
    }
    
    private void addMenuBorder() {
        ItemStack filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(MessageUtils.parse(" ")).build();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        try {
            int slot = event.getSlot();
            int[] slots = {11, 12, 13, 14, 15};
            
            int index = -1;
            for (int i = 0; i < slots.length; i++) {
                if (slots[i] == slot) {
                    index = i;
                    break;
                }
            }
            
            if (index == -1 || index >= attributes.size()) return;
            
            String attrId = attributes.get(index);
            MidgardProfile profile = MidgardCore.getProfileManager().getProfile(player);
            if (profile == null) return;
            ClassData data = profile.getData(ClassData.class);
            if (data == null) return;
            
            if (data.getAttributePoints() > 0) {
                // Spend point
                data.setAttributePoints(data.getAttributePoints() - 1);
                data.addSpentPoints(attrId, 1);
                
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                
                // Send attribute increase message
                if (ClassesModule.getInstance() != null) {
                    Attribute attr = AttributeRegistry.getInstance().getAttribute(attrId);
                    String attrMsg = ClassesModule.getInstance().getMessage("attributes.points_spent")
                        .replace("%attribute%", attr != null ? attr.getName() : attrId)
                        .replace("%points%", "1")
                        .replace("%remaining%", String.valueOf(data.getAttributePoints()));
                    MessageUtils.send(player, attrMsg);
                }
                
                // Recalculate
                if (module.getClassManager() != null && data.getClassName() != null) {
                    RPGClass rpgClass = module.getClassManager().getClass(data.getClassName());
                    if (rpgClass != null) {
                        module.applyClassAttributes(profile, rpgClass, data.getLevel());
                    }
                }
                
                // Refresh GUI
                initializeItems();
            } else {
                String errorMsg = module.getMessage("errors.no_points");
                MessageUtils.send(player, errorMsg);
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        } catch (Exception e) {
            ClassesModule.getInstance().getPlugin().getLogger().log(java.util.logging.Level.SEVERE, "Erro ao processar clique na distribuição de atributos", e);
            player.sendMessage("§cOcorreu um erro ao distribuir pontos.");
        }
    }
}
