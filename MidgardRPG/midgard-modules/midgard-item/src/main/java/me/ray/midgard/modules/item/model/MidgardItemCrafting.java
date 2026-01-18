package me.ray.midgard.modules.item.model;

import org.bukkit.configuration.ConfigurationSection;
import java.util.HashMap;
import java.util.Map;

/**
 * Gerencia as configurações de crafting de um item.
 * <p>
 * Permite definir receitas de crafting (shaped/shapeless), ingredientes e resultados.
 */
public class MidgardItemCrafting {

    private final MidgardItemImpl item;
    private final ConfigurationSection base;

    /**
     * Construtor do MidgardItemCrafting.
     *
     * @param item O item pai.
     * @param base A seção de configuração base.
     */
    public MidgardItemCrafting(MidgardItemImpl item, ConfigurationSection base) {
        this.item = item;
        this.base = base;
    }

    public boolean isCraftingEnabled() { return base.getBoolean("crafting.enabled", false); }
    public void setCraftingEnabled(boolean val) { base.set("crafting.enabled", val); item.save(); }

    public boolean isCraftingEnabled(String type) { return base.getBoolean("crafting." + type.toLowerCase() + ".enabled", false); }
    public void setCraftingEnabled(String type, boolean val) { base.set("crafting." + type.toLowerCase() + ".enabled", val); item.save(); }

    public boolean isCraftingShaped() { return base.getBoolean("crafting.shaped", true); }
    public void setCraftingShaped(boolean val) { base.set("crafting.shaped", val); item.save(); }

    public boolean isCraftingShaped(String type) { return base.getBoolean("crafting." + type.toLowerCase() + ".shaped", true); }
    public void setCraftingShaped(String type, boolean val) { base.set("crafting." + type.toLowerCase() + ".shaped", val); item.save(); }

    public int getCraftingOutputAmount() { return base.getInt("crafting.output", 1); }
    public void setCraftingOutputAmount(int val) { base.set("crafting.output", val); item.save(); }

    public int getCraftingOutputAmount(String type) { return base.getInt("crafting." + type.toLowerCase() + ".output", 1); }
    public void setCraftingOutputAmount(String type, int val) { base.set("crafting." + type.toLowerCase() + ".output", val); item.save(); }

    public Map<Integer, String> getCraftingIngredients() {
        Map<Integer, String> ingredients = new HashMap<>();
        if (base.isConfigurationSection("crafting.ingredients")) {
            ConfigurationSection section = base.getConfigurationSection("crafting.ingredients");
            for (String key : section.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(key);
                    ingredients.put(slot, section.getString(key));
                } catch (NumberFormatException e) {
                    org.bukkit.Bukkit.getLogger().warning("Slot de crafting inválido encontrado em '" + item.getId() + "': " + key);
                }
            }
        }
        return ingredients;
    }

    public Map<Integer, String> getCraftingIngredients(String type) {
        Map<Integer, String> ingredients = new HashMap<>();
        String path = "crafting." + type.toLowerCase() + ".ingredients";
        if (base.isConfigurationSection(path)) {
            ConfigurationSection section = base.getConfigurationSection(path);
            for (String key : section.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(key);
                    ingredients.put(slot, section.getString(key));
                } catch (NumberFormatException e) {
                    org.bukkit.Bukkit.getLogger().warning("Slot de crafting (" + type + ") inválido encontrado em '" + item.getId() + "': " + key);
                }
            }
        }
        return ingredients;
    }

    public void setCraftingIngredient(int slot, String ingredient) {
        if (ingredient == null) {
            base.set("crafting.ingredients." + slot, null);
        } else {
            base.set("crafting.ingredients." + slot, ingredient);
        }
        item.save();
    }

    public void setCraftingIngredient(String type, int slot, String ingredient) {
        String path = "crafting." + type.toLowerCase() + ".ingredients." + slot;
        if (ingredient == null) {
            base.set(path, null);
        } else {
            base.set(path, ingredient);
        }
        item.save();
    }

    public double getCraftingExperience(String type) { return base.getDouble("crafting." + type.toLowerCase() + ".experience", 0.0); }
    public void setCraftingExperience(String type, double val) { base.set("crafting." + type.toLowerCase() + ".experience", val); item.save(); }

    public int getCraftingDuration(String type) { return base.getInt("crafting." + type.toLowerCase() + ".duration", 200); }
    public void setCraftingDuration(String type, int val) { base.set("crafting." + type.toLowerCase() + ".duration", val); item.save(); }

    public boolean isCraftingHiddenFromBook(String type) { return base.getBoolean("crafting." + type.toLowerCase() + ".hide-book", false); }
    public void setCraftingHiddenFromBook(String type, boolean val) { base.set("crafting." + type.toLowerCase() + ".hide-book", val); item.save(); }
}
