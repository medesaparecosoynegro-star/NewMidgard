package me.ray.midgard.core.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder name(Component name) {
        meta.displayName(name);
        return this;
    }

    public ItemBuilder lore(Component... lore) {
        meta.lore(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        meta.lore(lore);
        return this;
    }

    public ItemBuilder setName(String name) {
        meta.displayName(me.ray.midgard.core.text.MessageUtils.parse(name));
        return this;
    }

    public ItemBuilder addLore(String line) {
        List<Component> lore = meta.lore();
        if (lore == null) lore = new ArrayList<>();
        lore.add(me.ray.midgard.core.text.MessageUtils.parse(line));
        meta.lore(lore);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemBuilder customModelData(int data) {
        meta.setCustomModelData(data);
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        meta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder glow() {
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder glowIf(boolean condition) {
        if (condition) return glow();
        return this;
    }

    public ItemBuilder color(Color color) {
        if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).setColor(color);
        }
        return this;
    }

    public ItemBuilder skullOwner(OfflinePlayer player) {
        if (meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwningPlayer(player);
        }
        return this;
    }



    public <T, Z> ItemBuilder pdc(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        meta.getPersistentDataContainer().set(key, type, value);
        return this;
    }

    public ItemBuilder editMeta(Consumer<ItemMeta> metaConsumer) {
        metaConsumer.accept(meta);
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder addLoreLine(Component line) {
        List<Component> currentLore = meta.lore();
        if (currentLore == null) {
            currentLore = new ArrayList<>();
        }
        currentLore.add(line);
        meta.lore(currentLore);
        return this;
    }

    public ItemBuilder addLoreLine(String line) {
        return addLoreLine(Component.text(line));
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
