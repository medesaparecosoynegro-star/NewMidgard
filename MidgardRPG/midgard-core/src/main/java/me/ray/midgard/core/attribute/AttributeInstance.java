package me.ray.midgard.core.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AttributeInstance {

    private final Attribute attribute;
    private double baseValue;
    private final List<AttributeModifier> modifiers = new ArrayList<>();
    private double cachedValue;
    private boolean dirty = true;

    public AttributeInstance(Attribute attribute) {
        this.attribute = attribute;
        this.baseValue = attribute.getBaseValue();
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public double getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(double baseValue) {
        this.baseValue = baseValue;
        this.dirty = true;
    }

    public void addModifier(AttributeModifier modifier) {
        modifiers.add(modifier);
        dirty = true;
    }

    public void removeModifier(AttributeModifier modifier) {
        modifiers.remove(modifier);
        dirty = true;
    }
    
    public void removeModifier(UUID uuid) {
        modifiers.removeIf(m -> m.getUuid().equals(uuid));
        dirty = true;
    }

    public void removeModifier(String name) {
        modifiers.removeIf(m -> m.getName().equals(name));
        dirty = true;
    }

    public double getValue() {
        if (dirty) {
            calculateValue();
        }
        return cachedValue;
    }

    private void calculateValue() {
        double value = baseValue;
        double additive = 0;
        double scalar = 1;

        for (AttributeModifier modifier : modifiers) {
            if (modifier.getOperation() == AttributeOperation.ADD_NUMBER) {
                value += modifier.getAmount();
            } else if (modifier.getOperation() == AttributeOperation.MULTIPLY_PERCENTAGE_ADDITIVE) {
                additive += modifier.getAmount();
            } else if (modifier.getOperation() == AttributeOperation.MULTIPLY_SCALAR) {
                scalar *= modifier.getAmount();
            }
        }

        value = value * (1 + additive) * scalar;
        
        // Clamp
        value = Math.max(attribute.getMinValue(), Math.min(attribute.getMaxValue(), value));
        
        this.cachedValue = value;
        this.dirty = false;
    }
}
