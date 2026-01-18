package me.ray.midgard.loader.gui;

import me.ray.midgard.core.attribute.Attribute;
import me.ray.midgard.core.attribute.AttributeInstance;
import me.ray.midgard.core.attribute.AttributeRegistry;
import me.ray.midgard.core.attribute.CoreAttributeData;
import me.ray.midgard.core.config.ConfigWrapper;
import me.ray.midgard.core.gui.BaseGui;
import me.ray.midgard.core.gui.MenuLoader;
import me.ray.midgard.core.profile.MidgardProfile;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class StatsGui extends BaseGui {

    private final MidgardProfile targetProfile;
    private final ConfigWrapper config;
    private final Map<Integer, String> actions = new HashMap<>();

    public StatsGui(org.bukkit.entity.Player viewer, JavaPlugin plugin, MidgardProfile targetProfile) {
        super(
            viewer,
            new ConfigWrapper(plugin, "modules/character/guis/stats.yml").getConfig().getInt("size", 54) / 9,
            // We need to resolve the title key here manually because super() is called first
            resolveTitle(viewer, new ConfigWrapper(plugin, "modules/character/guis/stats.yml").getConfig().getString("title", "Stats"), targetProfile)
        );
        this.targetProfile = targetProfile;
        this.config = new ConfigWrapper(plugin, "modules/character/guis/stats.yml"); // Fixed path
    }

    private static String resolveTitle(org.bukkit.entity.Player viewer, String titleKey, MidgardProfile target) {
        if (titleKey.startsWith("key:")) {
            titleKey = me.ray.midgard.core.MidgardCore.getLanguageManager().getRawMessage(titleKey.substring(4));
        }
        return titleKey.replace("%player%", target.getName());
    }

    @Override
    public void initializeItems() {
        MenuLoader.loadItems(player, inventory, config.getConfig().getConfigurationSection("items"), actions, this::replacePlaceholders);
    }

    private String replacePlaceholders(String text) {
        if (text == null) return null;
        
        text = text.replace("%player%", targetProfile.getName());
        // Add level and class placeholders when available
        text = text.replace("%level%", "1"); 
        text = text.replace("%class%", "Guerreiro");

        if (targetProfile.hasData(CoreAttributeData.class)) {
            CoreAttributeData data = targetProfile.getData(CoreAttributeData.class);
            
            if (text.contains("%stat_")) {
                for (Attribute attribute : AttributeRegistry.getInstance().getAll()) {
                    String placeholder = "%stat_" + attribute.getId() + "%";
                    if (text.contains(placeholder)) {
                        AttributeInstance instance = data.getInstance(attribute);
                        String format = attribute.getFormat();
                        String valueStr;
                        try {
                            valueStr = new DecimalFormat(format).format(instance.getValue());
                        } catch (Exception e) {
                            valueStr = String.valueOf(instance.getValue());
                        }
                        text = text.replace(placeholder, valueStr);
                    }
                }
            }
        }
        
        return text;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        String action = actions.get(event.getSlot());
        if (action != null) {
            if (action.equalsIgnoreCase("close")) {
                event.getWhoClicked().closeInventory();
            }
        }
    }
}
