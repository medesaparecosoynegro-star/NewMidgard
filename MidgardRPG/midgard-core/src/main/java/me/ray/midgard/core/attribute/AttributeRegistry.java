package me.ray.midgard.core.attribute;

import me.ray.midgard.core.registry.Registry;

public class AttributeRegistry extends Registry<String, Attribute> {
    
    private static final AttributeRegistry INSTANCE = new AttributeRegistry();
    
    public static AttributeRegistry getInstance() {
        return INSTANCE;
    }
    
    private AttributeRegistry() {}

    public Attribute getAttribute(String id) {
        return get(id).orElse(null);
    }
}
