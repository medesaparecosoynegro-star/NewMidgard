package me.ray.midgard.modules.classes.gui;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.config.ConfigWrapper;
import me.ray.midgard.core.gui.BaseGui;
import me.ray.midgard.core.profile.MidgardProfile;
import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.modules.classes.ClassData;
import me.ray.midgard.modules.classes.ClassesModule;
import me.ray.midgard.modules.classes.RPGClass;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class ClassSelectionGui extends BaseGui {

    private final ClassesModule module;
    private final ConfigWrapper config;
    private final JavaPlugin plugin;
    private final Map<Integer, String> actions = new HashMap<>();

    private static ConfigWrapper loadConfig(JavaPlugin plugin) {
        return new ConfigWrapper(plugin, "modules/classes/guis/selection.yml");
    }

    public ClassSelectionGui(JavaPlugin plugin, Player player, ClassesModule module) {
        this(plugin, player, module, loadConfig(plugin));
    }

    private ClassSelectionGui(JavaPlugin plugin, Player player, ClassesModule module, ConfigWrapper config) {
        super(
            player,
            config.getConfig().getInt("size", 27) / 9,
            config.getConfig().getString("title", "Seleção de Classe")
        );
        this.plugin = plugin;
        this.module = module;
        this.config = config;
    }

    @Override
    public void initializeItems() {
        try {
            ConfigurationSection itemsSection = config.getConfig().getConfigurationSection("items");
            if (itemsSection == null) {
                plugin.getLogger().warning("Seção 'items' não encontrada em selection.yml");
                return;
            }

            me.ray.midgard.core.gui.MenuLoader.loadItems(player, inventory, itemsSection, actions, null);
        } catch (Exception e) {
             plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao inicializar itens da GUI de seleção de classe", e);
        }
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null) return;

        try {
            String action = actions.get(event.getSlot());
            if (action == null) return;
    
            Player player = (Player) event.getWhoClicked();
    
            if (action.startsWith("select_class:")) {
                String classId = action.split(":")[1].trim();
                selectClass(player, classId);
            }
        } catch (Exception e) {
             plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao processar clique na GUI de seleção de classe", e);
             player.sendMessage("§cOcorreu um erro ao selecionar a classe.");
        }
    }

    @Override
    public void onClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
        try {
            Player player = (Player) event.getPlayer();
            MidgardProfile profile = MidgardCore.getProfileManager().getProfile(player);
    
            if (profile != null) {
                ClassData data = profile.getData(ClassData.class);
                // If data is null or className is null, they haven't selected a class
                if (data == null || data.getClassName() == null) {
                    // Reopen in next tick to avoid event conflicts
                    org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline()) {
                            // Re-open safe
                            try {
                                new ClassSelectionGui(plugin, player, module).open();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 1L);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao fechar GUI de seleção de classe", e);
        }
    }

    private void selectClass(Player player, String classId) {
        try {
            if (module.getClassManager() == null) {
                player.sendMessage("§cErro interno: Gerenciador de classes não inicializado.");
                return;
            }
            RPGClass rpgClass = module.getClassManager().getClass(classId);
            if (rpgClass == null) {
                String errorMsg = module.getMessage("errors.class_not_found")
                    .replace("%class%", classId);
                MessageUtils.send(player, errorMsg);
                return;
            }
    
            MidgardProfile profile = MidgardCore.getProfileManager().getProfile(player);
            if (profile == null) return;
    
            ClassData data = profile.getOrCreateData(ClassData.class);
            
            // Logic to set class
            data.setClassName(classId);
            data.setLevel(1);
            data.setExperience(0);
            
            module.applyClassAttributes(profile, rpgClass, 1);
            
            // Send selection success message
            String successMsg = module.getMessage("class.selected")
                .replace("%class%", rpgClass.getDisplayName())
                .replace("%class_name%", rpgClass.getDisplayName());
            MessageUtils.send(player, successMsg);
            
            // Send welcome message with class info
            String infoMsg = module.getMessage("class.welcome")
                .replace("%class%", rpgClass.getDisplayName())
                .replace("%class_name%", rpgClass.getDisplayName())
                .replace("%description%", rpgClass.getLore() != null && !rpgClass.getLore().isEmpty() ? 
                    String.join(" ", rpgClass.getLore()) : "Classe poderosa");
            MessageUtils.send(player, infoMsg);
            
            player.closeInventory();
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao selecionar classe " + classId, e);
            player.sendMessage("§cErro ao confirmar seleção de classe.");
        }
    }
}
