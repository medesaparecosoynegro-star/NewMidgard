package me.ray.midgard.modules.character.gui;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.attribute.AttributeInstance;
import me.ray.midgard.core.attribute.CoreAttributeData;
import me.ray.midgard.core.gui.BaseGui;
import me.ray.midgard.core.profile.MidgardProfile;
import me.ray.midgard.core.utils.ItemBuilder;
import me.ray.midgard.modules.character.CharacterModule;
import me.ray.midgard.modules.classes.ClassData;
import me.ray.midgard.modules.classes.ClassesModule;
import me.ray.midgard.modules.combat.CombatAttributes;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharacterMenu extends BaseGui {

    private int statsPage = 1;
    private final int MAX_STATS_PER_PAGE = 10;

    public CharacterMenu(Player player) {
        super(player, 
              CharacterModule.getInstance().getCharacterConfig().getInt("menu.rows", 3), 
              CharacterModule.getInstance().getCharacterConfig().getString("menu.title", "Informações do Personagem"));
        
        // Send menu opening message
        String openMsg = CharacterModule.getInstance().getMessage("menu.opening");
        if (openMsg != null && !openMsg.isEmpty() && !openMsg.equals("menu.opening")) {
            me.ray.midgard.core.text.MessageUtils.send(player, openMsg);
        }
    }

    @Override
    public void initializeItems() {
        try {
            FileConfiguration config = CharacterModule.getInstance().getCharacterConfig();
            if (config == null) return;
            
            MidgardProfile profile = MidgardCore.getProfileManager().getProfile(player);
            if (profile == null) return;
            
            ClassData classData = profile.getData(ClassData.class);
            int availablePoints = classData != null ? classData.getAttributePoints() : 0;
            int usedPoints = classData != null ? classData.getSpentPoints().values().stream().mapToInt(Integer::intValue).sum() : 0;
            int maxPoints = availablePoints + usedPoints;
            int level = classData != null ? classData.getLevel() : 1;

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("%player_name%", player.getName() != null ? player.getName() : "Unknown");
            placeholders.put("%class_name%", (classData != null && classData.getClassName() != null) ? classData.getClassName() : "Nenhuma");
            placeholders.put("%available_points%", String.valueOf(availablePoints));
            placeholders.put("%max_points%", String.valueOf(maxPoints));
            placeholders.put("%class_level%", String.valueOf(level));
            placeholders.put("%next_level%", String.valueOf(level + 1));
            
            if (player.getHealth() > 0) {
                 placeholders.put("%health%", String.valueOf((int)player.getHealth()));
            } else {
                 placeholders.put("%health%", "0");
            }
            
            if (player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue() > 0) {
                placeholders.put("%max_health%", String.valueOf((int)player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue()));
            } else {
                placeholders.put("%max_health%", "20");
            }

            // Attributes
            addStatsPlaceholders(placeholders, classData, CombatAttributes.STRENGTH, "strength");
            addStatsPlaceholders(placeholders, classData, CombatAttributes.DEXTERITY, "dexterity");
            addStatsPlaceholders(placeholders, classData, CombatAttributes.INTELLIGENCE, "intelligence");
            addStatsPlaceholders(placeholders, classData, CombatAttributes.DEFENSE, "defense");
            addStatsPlaceholders(placeholders, classData, CombatAttributes.AGILITY, "agility");

            // Attribute Values (Total)
            addAttributePlaceholder(placeholders, profile, "%strength%", CombatAttributes.STRENGTH);
            addAttributePlaceholder(placeholders, profile, "%dexterity%", CombatAttributes.DEXTERITY);
            addAttributePlaceholder(placeholders, profile, "%intelligence%", CombatAttributes.INTELLIGENCE);
            addAttributePlaceholder(placeholders, profile, "%defense%", CombatAttributes.DEFENSE);
            addAttributePlaceholder(placeholders, profile, "%agility%", CombatAttributes.AGILITY);
            
            // Defenses
            addAttributePlaceholder(placeholders, profile, "%defense_earth%", CombatAttributes.EARTH_DEFENSE);
            addAttributePlaceholder(placeholders, profile, "%defense_thunder%", CombatAttributes.THUNDER_DEFENSE);
            addAttributePlaceholder(placeholders, profile, "%defense_water%", CombatAttributes.WATER_DEFENSE);
            addAttributePlaceholder(placeholders, profile, "%defense_fire%", CombatAttributes.FIRE_DEFENSE);
            addAttributePlaceholder(placeholders, profile, "%defense_air%", CombatAttributes.AIR_DEFENSE);
            addAttributePlaceholder(placeholders, profile, "%defense_ice%", CombatAttributes.ICE_DEFENSE);
            addAttributePlaceholder(placeholders, profile, "%defense_light%", CombatAttributes.LIGHT_DEFENSE);
            addAttributePlaceholder(placeholders, profile, "%defense_darkness%", CombatAttributes.DARKNESS_DEFENSE);
            addAttributePlaceholder(placeholders, profile, "%defense_divine%", CombatAttributes.DIVINE_DEFENSE);

            // Elemental Damage
            addAttributePlaceholder(placeholders, profile, "%damage_earth%", CombatAttributes.EARTH_DAMAGE);
            addAttributePlaceholder(placeholders, profile, "%damage_thunder%", CombatAttributes.THUNDER_DAMAGE);
            addAttributePlaceholder(placeholders, profile, "%damage_water%", CombatAttributes.WATER_DAMAGE);
            addAttributePlaceholder(placeholders, profile, "%damage_fire%", CombatAttributes.FIRE_DAMAGE);
            addAttributePlaceholder(placeholders, profile, "%damage_air%", CombatAttributes.AIR_DAMAGE);
            addAttributePlaceholder(placeholders, profile, "%damage_ice%", CombatAttributes.ICE_DAMAGE);
            addAttributePlaceholder(placeholders, profile, "%damage_light%", CombatAttributes.LIGHT_DAMAGE);
            addAttributePlaceholder(placeholders, profile, "%damage_darkness%", CombatAttributes.DARKNESS_DAMAGE);
            addAttributePlaceholder(placeholders, profile, "%damage_divine%", CombatAttributes.DIVINE_DAMAGE);

            // Other Stats
            addAttributePlaceholder(placeholders, profile, "%health_regen%", CombatAttributes.HEALTH_REGEN);
            addAttributePlaceholder(placeholders, profile, "%mana_regen%", CombatAttributes.MANA_REGEN);
            addAttributePlaceholder(placeholders, profile, "%life_steal%", CombatAttributes.LIFE_STEAL);
            addAttributePlaceholder(placeholders, profile, "%speed%", CombatAttributes.SPEED);
            addAttributePlaceholder(placeholders, profile, "%spell_damage%", CombatAttributes.SKILL_DAMAGE);
            addAttributePlaceholder(placeholders, profile, "%weapon_damage%", CombatAttributes.WEAPON_DAMAGE);
            addAttributePlaceholder(placeholders, profile, "%main_attack_damage%", CombatAttributes.WEAPON_DAMAGE); // Alias
            addAttributePlaceholder(placeholders, profile, "%damage%", CombatAttributes.PHYSICAL_DAMAGE); // Correct mapping for %damage%

            // Custom/Derived
            addAttributePlaceholder(placeholders, profile, "%xp_bonus%", CombatAttributes.XP_BONUS);
            addAttributePlaceholder(placeholders, profile, "%loot_bonus%", CombatAttributes.LOOT_BONUS);
            addAttributePlaceholder(placeholders, profile, "%mana_steal%", CombatAttributes.MANA_STEAL);
            
            // Quests & XP
            placeholders.put("%quests_completed%", "0"); 
            placeholders.put("%quests_total%", "100");
            placeholders.put("%xp_percent%", "100%");

            if (config.isConfigurationSection("menu.items")) {
                for (String key : config.getConfigurationSection("menu.items").getKeys(false)) {
                    String path = "menu.items." + key;
                    int slot = config.getInt(path + ".slot");
                    if (slot >= 0 && slot < inventory.getSize()) {
                        try {
                            inventory.setItem(slot, buildItemFromConfig(config, path, placeholders));
                        } catch (Exception e) {
                            CharacterModule.getInstance().getPlugin().getLogger().warning("Erro ao construir item do menu '" + key + "': " + e.getMessage());
                        }
                    }
                }
            }
                    
            fillEmpty();
        } catch (Exception e) {
            CharacterModule.getInstance().getPlugin().getLogger().log(java.util.logging.Level.SEVERE, "Erro crítico ao inicializar o CharacterMenu", e);
        }
    }
    
    private void addAttributePlaceholder(Map<String, String> map, MidgardProfile profile, String placeholder, String attribute) {
        double val = getAttributeValue(profile, attribute);
        // Format: +Value (green) or -Value (red)
        String color = (val >= 0) ? "&a+" : "&c";
        
        // Remove .0 if it's an integer value, otherwise keep 1 decimal place
        String numStr = (val % 1 == 0) ? String.valueOf((int)val) : String.format("%.1f", val);
        
        // Final string: e.g. "&a+10" OR "&c-5"
        map.put(placeholder, color + numStr);
    }

    private void addStatsPlaceholders(Map<String, String> map, ClassData data, String attrId, String keyPrefix) {
        int pts = getSpentPoints(data, attrId);
        map.put("%" + keyPrefix + "_pts%", String.valueOf(pts));
        map.put("%" + keyPrefix + "_pts_next%", String.valueOf(pts + 1));
    }
    
    private ItemStack buildItemFromConfig(FileConfiguration config, String path, Map<String, String> placeholders) {
        String matName = config.getString(path + ".material", "STONE");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.STONE;
        
        ItemBuilder builder = new ItemBuilder(mat);
        
        String name = config.getString(path + ".name");
        if (name != null) {
            builder.setName(applyPlaceholders(name, placeholders));
        }

        // Special handling for paginated lore using placeholder
        if (config.contains(path + ".identifications") && config.contains(path + ".lore")) {
            List<String> rawLore = config.getStringList(path + ".lore");
            List<String> ids = config.getStringList(path + ".identifications");
            
            // Pagination Calculations
            int totalStats = ids.size();
            int maxPages = (int) Math.ceil((double) totalStats / MAX_STATS_PER_PAGE);
            if (maxPages < 1) maxPages = 1;
            if (statsPage > maxPages) statsPage = 1;
            
            int startIndex = (statsPage - 1) * MAX_STATS_PER_PAGE;
            int endIndex = Math.min(startIndex + MAX_STATS_PER_PAGE, totalStats);
            
            placeholders.put("%page%", String.valueOf(statsPage));
            placeholders.put("%max_pages%", String.valueOf(maxPages));
            
            for (String line : rawLore) {
                if (line.trim().equals("%identifications_page%")) {
                    // Inject current page items
                    if (totalStats == 0) continue; // Skip if no stats

                    for (int i = startIndex; i < endIndex; i++) {
                        String rawId = ids.get(i);
                        // Process placeholders in the ID line
                        // Example rawId: "&d- &7Strength: %strength%"
                        // The placeholder value already includes color code like "&a+10"
                        builder.addLore(applyPlaceholders(rawId, placeholders));
                    }
                } else if (line.trim().equals("%pagination%")) {
                    if (maxPages > 1) {
                        List<String> format = config.getStringList("menu.items.character_info.pagination.format");
                        
                        // Default fallback if not configured
                        if (format.isEmpty()) {
                            format = List.of(
                                "",
                                "&fPágina %current_page%",
                                "&6&l<< %bar%&6&l>>"
                            );
                        }

                        StringBuilder bar = new StringBuilder();
                        String currentSymbol = config.getString("menu.items.character_info.pagination.bar.current", "&a&l■ ");
                        String otherSymbol = config.getString("menu.items.character_info.pagination.bar.other", "&7&l■ ");
                        
                        for (int p = 1; p <= maxPages; p++) {
                             if (p == statsPage) {
                                 bar.append(currentSymbol);
                             } else {
                                 bar.append(otherSymbol);
                             }
                        }

                        for (String fmtLine : format) {
                            String processedLine = fmtLine
                                .replace("%current_page%", String.valueOf(statsPage))
                                .replace("%total_pages%", String.valueOf(maxPages))
                                .replace("%bar%", bar.toString());
                            builder.addLore(processedLine);
                        }
                    }
                } else {
                    builder.addLore(applyPlaceholders(line, placeholders));
                }
            }
        } 
        // Standard Lore
        else if (config.contains(path + ".lore")) {
            List<String> lore = config.getStringList(path + ".lore");
            if (!lore.isEmpty()) {
                for (String line : lore) {
                    builder.addLore(applyPlaceholders(line, placeholders));
                }
            }
        }
        
        if (config.contains(path + ".skull_owner")) {
            String owner = applyPlaceholders(config.getString(path + ".skull_owner"), placeholders);
            builder.skullOwner(org.bukkit.Bukkit.getOfflinePlayer(owner));
        }
        
        if (config.contains(path + ".flags")) {
            List<String> flags = config.getStringList(path + ".flags");
            for (String flagName : flags) {
                try {
                    builder.flags(ItemFlag.valueOf(flagName));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        
        return builder.build();
    }
    
    private String applyPlaceholders(String text, Map<String, String> placeholders) {
        if (text == null) return null;
        String result = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    private int getSpentPoints(ClassData data, String attr) {
        if (data == null) return 0;
        return data.getSpentPoints(attr);
    }
    
    private double getAttributeValue(MidgardProfile profile, String attr) {
        if (profile == null) return 0;
        CoreAttributeData data = profile.getData(CoreAttributeData.class);
        if (data == null) return 0;
        AttributeInstance instance = data.getInstance(attr);
        return instance != null ? instance.getValue() : 0;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        try {
            int slot = event.getSlot();
            FileConfiguration config = CharacterModule.getInstance().getCharacterConfig();
            if (config == null) return;
            
            // Pagination Click
            if (slot == config.getInt("menu.items.character_info.slot", 7)) {
                if (config.contains("menu.items.character_info.identifications")) {
                    List<?> list = config.getList("menu.items.character_info.identifications");
                    if (list != null) {
                        int totalStats = list.size();
                        int maxPages = (int) Math.ceil((double) totalStats / MAX_STATS_PER_PAGE);
                        
                        if (maxPages > 1) {
                            statsPage++;
                            if (statsPage > maxPages) {
                                statsPage = 1;
                            }
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                            initializeItems();
                            return;
                        }
                    }
                }
            }

            String attribute = null;
            
            if (slot == config.getInt("menu.items.strength.slot")) attribute = CombatAttributes.STRENGTH;
            else if (slot == config.getInt("menu.items.dexterity.slot")) attribute = CombatAttributes.DEXTERITY;
            else if (slot == config.getInt("menu.items.intelligence.slot")) attribute = CombatAttributes.INTELLIGENCE;
            else if (slot == config.getInt("menu.items.defense.slot")) attribute = CombatAttributes.DEFENSE;
            else if (slot == config.getInt("menu.items.agility.slot")) attribute = CombatAttributes.AGILITY;
            
            if (attribute != null) {
                MidgardProfile profile = MidgardCore.getProfileManager().getProfile(player);
                if (profile == null) return;
                ClassData data = profile.getData(ClassData.class);
                if (data == null) return;
                
                int amount = event.isShiftClick() ? 5 : 1;
                if (data.getAttributePoints() >= amount) {
                    // Remove points
                    data.setAttributePoints(data.getAttributePoints() - amount);
                    // Add points to attribute
                    data.addSpentPoints(attribute, amount);
                    
                    // Recalculate stats
                    ClassesModule classesModule = ClassesModule.getInstance();
                    if (classesModule != null && data.getClassName() != null) {
                        me.ray.midgard.modules.classes.ClassManager classManager = classesModule.getClassManager();
                        if (classManager != null) {
                             me.ray.midgard.modules.classes.RPGClass rpgClass = classManager.getClass(data.getClassName());
                             if (rpgClass != null) {
                                  classesModule.applyClassAttributes(profile, rpgClass, data.getLevel());
                             }
                        }
                    }
                    
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    initializeItems(); // Refresh GUI
                } else {
                    player.sendMessage("§cVocê não tem pontos de atributos suficientes!");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                }
            }
        } catch (Exception e) {
             CharacterModule.getInstance().getPlugin().getLogger().log(java.util.logging.Level.SEVERE, "Erro ao processar clique no CharacterMenu", e);
             player.sendMessage("§cOcorreu um erro ao processar sua ação.");
        }
    }
    
    private void fillEmpty() {
        FileConfiguration config = CharacterModule.getInstance().getCharacterConfig();
        if (!config.getBoolean("menu.filler.enabled", true)) {
            return;
        }

        String path = "menu.filler";
        Map<String, String> emptyPlaceholders = new HashMap<>(); // No placeholders for filler
        
        ItemStack filler = buildItemFromConfig(config, path, emptyPlaceholders);
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                inventory.setItem(i, filler);
            }
        }
    }
}
