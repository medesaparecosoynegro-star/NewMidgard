package me.ray.midgard.core.integration;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemsAdderUtils {

    @Nullable
    public static ItemStack getCustomItem(String id) {
        CustomStack stack = CustomStack.getInstance(id);
        return stack != null ? stack.getItemStack() : null;
    }

    public static boolean isCustomItem(ItemStack item) {
        return CustomStack.byItemStack(item) != null;
    }

    @Nullable
    public static String getCustomItemId(ItemStack item) {
        CustomStack stack = CustomStack.byItemStack(item);
        return stack != null ? stack.getNamespacedID() : null;
    }
}
