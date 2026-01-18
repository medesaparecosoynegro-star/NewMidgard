package me.ray.midgard.modules.classes;

import java.util.List;
import java.util.Map;

/**
 * Representa uma classe (profissão) no RPG.
 * Define os atributos base e o crescimento de atributos por nível.
 */
public class RPGClass {

    private final String id;
    private final String displayName;
    private final String icon; // Material name
    private final List<String> lore;
    private final Map<String, Double> baseAttributes;
    private final Map<String, Double> attributesPerLevel;
    private final double healthPerLevel;
    private final double manaPerLevel;
    private final double baseMana;
    private final double baseHealth;
    private final Map<String, Integer> skills; // Skill ID -> Undo Level

    /**
     * Construtor da RPGClass.
     *
     * @param id ID único da classe.
     * @param displayName Nome de exibição.
     * @param icon Ícone da classe (Material).
     * @param lore Descrição da classe.
     * @param baseAttributes Atributos iniciais.
     * @param attributesPerLevel Atributos ganhos por nível.
     * @param baseHealth Vida base.
     * @param healthPerLevel Vida ganha por nível.
     * @param baseMana Mana base.
     * @param manaPerLevel Mana ganha por nível.
     * @param skills Skills disponíveis e nível de desbloqueio.
     */
    public RPGClass(String id, String displayName, String icon, List<String> lore,
                   Map<String, Double> baseAttributes,
                   Map<String, Double> attributesPerLevel,
                   double baseHealth, double healthPerLevel, 
                   double baseMana, double manaPerLevel,
                   Map<String, Integer> skills) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.lore = lore;
        this.baseAttributes = baseAttributes;
        this.attributesPerLevel = attributesPerLevel;
        this.baseHealth = baseHealth;
        this.healthPerLevel = healthPerLevel;
        this.baseMana = baseMana;
        this.manaPerLevel = manaPerLevel;
        this.skills = skills;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
    
    public String getIcon() {
        return icon;
    }

    public List<String> getLore() {
        return lore;
    }

    public Map<String, Double> getBaseAttributes() {
        return baseAttributes;
    }

    public Map<String, Double> getAttributesPerLevel() {
        return attributesPerLevel;
    }

    public double getHealthPerLevel() {
        return healthPerLevel;
    }

    public double getManaPerLevel() {
        return manaPerLevel;
    }
    
    public double getBaseHealth() {
        return baseHealth;
    }
    
    public double getBaseMana() {
        return baseMana;
    }
    
    public Map<String, Integer> getSkills() {
        return skills;
    }
}
