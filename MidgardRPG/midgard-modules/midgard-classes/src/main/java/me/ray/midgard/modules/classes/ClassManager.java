package me.ray.midgard.modules.classes;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gerencia o carregamento e acesso às classes RPG.
 */
public class ClassManager {

    private final JavaPlugin plugin;
    private final Map<String, RPGClass> classes = new HashMap<>();
    private final File classesFolder;

    /**
     * Construtor do ClassManager.
     *
     * @param plugin Instância do plugin.
     */
    public ClassManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.classesFolder = new File(plugin.getDataFolder(), "modules/classes/classes");
        loadClasses();
    }

    /**
     * Recarrega as classes do disco.
     */
    public void reload() {
        loadClasses();
    }

    private void loadClasses() {
        classes.clear();
        
        try {
            if (!classesFolder.exists()) {
                classesFolder.mkdirs();
                // Save default classes
                saveDefaultClass("guerreiro");
                saveDefaultClass("mago");
                saveDefaultClass("arqueiro");
            }
    
            File[] files = classesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files == null) return;
    
            for (File file : files) {
                try {
                    String key = file.getName().replace(".yml", "");
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                    String displayName = config.getString("display-name", key);
                    String icon = config.getString("icon", "BARRIER");
                    List<String> lore = config.getStringList("lore");

                    Map<String, Double> baseAttributes = new HashMap<>();
                    Map<String, Double> perLevelAttributes = new HashMap<>();

                    // Core Attributes Section
                    if (config.isConfigurationSection("attributes.base")) {
                        ConfigurationSection baseAttrSection = config.getConfigurationSection("attributes.base");
                        for (String attr : baseAttrSection.getKeys(false)) {
                            baseAttributes.put(attr, baseAttrSection.getDouble(attr));
                        }
                    } else if (config.isConfigurationSection("attributes")) {
                         // Legacy/Simple support
                        ConfigurationSection attrSection = config.getConfigurationSection("attributes");
                        for (String attr : attrSection.getKeys(false)) {
                            if (!attr.equals("base") && !attr.equals("per-level")) {
                                baseAttributes.put(attr, attrSection.getDouble(attr));
                            }
                        }
                    }

                    if (config.isConfigurationSection("attributes.per-level")) {
                        ConfigurationSection levelAttrSection = config.getConfigurationSection("attributes.per-level");
                        for (String attr : levelAttrSection.getKeys(false)) {
                            perLevelAttributes.put(attr, levelAttrSection.getDouble(attr));
                        }
                    } else if (config.isConfigurationSection("per-level.attributes")) {
                         // Legacy support
                        ConfigurationSection levelAttrSection = config.getConfigurationSection("per-level.attributes");
                         for (String attr : levelAttrSection.getKeys(false)) {
                            perLevelAttributes.put(attr, levelAttrSection.getDouble(attr));
                        }
                    }

                    // Resources
                    double baseHealth = config.getDouble("health.base", 20);
                    double healthPerLevel = config.getDouble("health.per-level", 0);
                    // Legacy check
                    if (config.contains("per-level.health")) healthPerLevel = config.getDouble("per-level.health");

                    double baseMana = config.getDouble("mana.base", 20);
                    double manaPerLevel = config.getDouble("mana.per-level", 0);
                    // Legacy check
                    if (config.contains("per-level.mana")) manaPerLevel = config.getDouble("per-level.mana");
                    
                    // Skills
                    Map<String, Integer> skills = new HashMap<>();
                    if (config.isConfigurationSection("skills")) {
                        ConfigurationSection skillsSection = config.getConfigurationSection("skills");
                        for (String skillId : skillsSection.getKeys(false)) {
                            if (skillsSection.isInt(skillId)) {
                                skills.put(skillId, skillsSection.getInt(skillId));
                            } else if (skillsSection.isConfigurationSection(skillId)) {
                                skills.put(skillId, skillsSection.getInt(skillId + ".level", 1));
                            }
                        }
                    }

                    RPGClass rpgClass = new RPGClass(key, displayName, icon, lore, baseAttributes, perLevelAttributes, baseHealth, healthPerLevel, baseMana, manaPerLevel, skills);
                    classes.put(key, rpgClass);
                } catch (Exception e) {
                    plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro ao carregar classe do arquivo: " + file.getName(), e);
                }
            }
            plugin.getLogger().info("Foram carregadas " + classes.size() + " classes com sucesso.");
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Erro crítico ao carregar diretório de classes", e);
        }
    }

    private void saveDefaultClass(String name) {
        String resourcePath = "modules/classes/classes/" + name + ".yml";
        try {
            // Check if resource exists before saving to avoid NPE in some spigot versions if path wrong
            // Actually getResource returns null if not found
            if (plugin.getResource(resourcePath) != null) {
                // If file already exists, saveResource does nothing if replace=false, but good to be safe
                File dest = new File(classesFolder, name + ".yml");
                if (!dest.exists()) {
                    plugin.saveResource(resourcePath, false);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.WARNING, "Não foi possível salvar a classe padrão: " + name, e);
        }
    }

    /**
     * Obtém uma classe pelo ID.
     *
     * @param id ID da classe.
     * @return A classe RPG ou null se não encontrada.
     */
    public RPGClass getClass(String id) {
        return classes.get(id);
    }

    /**
     * Obtém todas as classes carregadas.
     *
     * @return Mapa de classes.
     */
    public Map<String, RPGClass> getClasses() {
        return classes;
    }
}
