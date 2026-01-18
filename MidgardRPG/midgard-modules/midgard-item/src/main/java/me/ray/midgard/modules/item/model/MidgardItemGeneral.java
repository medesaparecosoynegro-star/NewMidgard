package me.ray.midgard.modules.item.model;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import java.util.ArrayList;
import java.util.List;

public class MidgardItemGeneral {

    private final MidgardItemImpl item;
    private final ConfigurationSection base;

    private Material material;
    private String name;
    private final List<String> lore = new ArrayList<>();
    private String tier;
    private int revisionId;
    private int browserIndex;

    public MidgardItemGeneral(MidgardItemImpl item, ConfigurationSection base) {
        this.item = item;
        this.base = base;
        load();
    }

    private void load() {
        this.material = Material.valueOf(base.getString("material", "STONE").toUpperCase());
        // Load raw string, do not translate colors eagerly.
        this.name = base.getString("name", "&cItem Inválido");
        this.tier = base.getString("tier", "COMMON");
        this.revisionId = base.getInt("revision-id", 1);
        this.browserIndex = base.getInt("browser-index", 0);
        
        if (base.isList("lore")) {
            this.lore.addAll(base.getStringList("lore"));
        }
    }

    public Material getMaterial() { return material; }
    public void setMaterial(Material material) {
        this.material = material;
        base.set("material", material.name());
        item.save();
    }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        base.set("name", name);
        item.save();
    }

    public List<String> getLore() { return lore; }
    public void setLore(List<String> lore) {
        this.lore.clear();
        this.lore.addAll(lore);
        base.set("lore", lore);
        item.save();
    }

    public String getTier() { return tier; }
    public void setTier(String val) { this.tier = val; base.set("tier", val); item.save(); }

    public int getRevisionId() { return revisionId; }
    public void setRevisionId(int revisionId) {
        this.revisionId = revisionId;
        base.set("revision-id", revisionId);
        item.save();
    }

    public int getBrowserIndex() { return browserIndex; }
    public void setBrowserIndex(int val) { this.browserIndex = val; base.set("browser-index", val); item.save(); }
}
