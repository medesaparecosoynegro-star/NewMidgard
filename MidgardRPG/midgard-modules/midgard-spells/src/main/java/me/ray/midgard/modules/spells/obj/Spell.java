package me.ray.midgard.modules.spells.obj;

import java.util.List;

public class Spell {

    private final String id;
    private final String mythicSkillName;
    private final String displayName;
    private final List<String> lore;
    private final double cooldown; // seconds
    private final double manaCost;
    private final double staminaCost;

    
    public Spell(String id, String mythicSkillName, String displayName, List<String> lore, double cooldown, double manaCost, double staminaCost) {
        this.id = id;
        this.mythicSkillName = mythicSkillName;
        this.displayName = displayName;
        this.lore = lore;
        this.cooldown = cooldown;
        this.manaCost = manaCost;
        this.staminaCost = staminaCost;
    }

    public String getId() {
        return id;
    }

    public String getMythicSkillName() {
        return mythicSkillName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }


    public double getCooldown() {
        return cooldown;
    }

    public double getManaCost() {
        return manaCost;
    }

    public double getStaminaCost() {
        return staminaCost;
    }

}
