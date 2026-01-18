package me.ray.midgard.modules.classes;

import me.ray.midgard.core.profile.ModuleData;
import java.util.HashMap;
import java.util.Map;

public class ClassData implements ModuleData {

    private String className;
    private int level;
    private double experience;
    private int attributePoints;
    private Map<String, Integer> spentPoints;

    public ClassData() {
        this.className = null;
        this.level = 1;
        this.experience = 0;
        this.attributePoints = 0;
        this.spentPoints = new HashMap<>();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getExperience() {
        return experience;
    }

    public void setExperience(double experience) {
        this.experience = experience;
    }
    
    public int getAttributePoints() {
        return attributePoints;
    }

    public void setAttributePoints(int attributePoints) {
        this.attributePoints = attributePoints;
    }
    
    public void addAttributePoints(int amount) {
        this.attributePoints += amount;
    }

    public Map<String, Integer> getSpentPoints() {
        if (spentPoints == null) {
            spentPoints = new HashMap<>();
        }
        return spentPoints;
    }

    public void setSpentPoints(Map<String, Integer> spentPoints) {
        this.spentPoints = spentPoints != null ? spentPoints : new HashMap<>();
    }
    
    public int getSpentPoints(String attributeId) {
        if (attributeId == null) return 0;
        return getSpentPoints().getOrDefault(attributeId, 0);
    }
    
    public void addSpentPoints(String attributeId, int amount) {
        if (attributeId == null) return;
        getSpentPoints().put(attributeId, getSpentPoints(attributeId) + amount);
    }

    public boolean hasClass() {
        return className != null && !className.isEmpty();
    }
}
