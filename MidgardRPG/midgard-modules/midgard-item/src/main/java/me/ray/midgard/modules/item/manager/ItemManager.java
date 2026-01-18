package me.ray.midgard.modules.item.manager;

import me.ray.midgard.core.debug.DebugCategory;
import me.ray.midgard.core.debug.MidgardLogger;
import me.ray.midgard.modules.item.ItemModule;
import me.ray.midgard.modules.item.model.MidgardItem;
import me.ray.midgard.modules.item.model.MidgardItemImpl;
import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.text.MessageUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gerencia o carregamento, registro e acesso aos itens do MidgardRPG.
 */
public class ItemManager {

    private final ItemModule module;
    private final Map<String, MidgardItem> itemMap;

    /**
     * Construtor do ItemManager.
     *
     * @param module Instância do módulo de itens.
     */
    public ItemManager(ItemModule module) {
        this.module = module;
        this.itemMap = new HashMap<>();
    }

    /**
     * Carrega todos os itens da pasta de configuração.
     */
    public void loadItems() {
        itemMap.clear();
        File itemsFolder = new File(module.getDataFolder(), "item");
        if (!itemsFolder.exists()) {
            itemsFolder.mkdirs();
        }

        loadItemsFromFolder(itemsFolder);
        MidgardLogger.info("Carregados " + itemMap.size() + " itens.");
        MidgardLogger.debug(DebugCategory.ITEMS, "Carregamento de itens concluído. Total: %d", itemMap.size());
    }

    /**
     * Registra um item manualmente.
     *
     * @param item Item a ser registrado.
     */
    public void registerItem(MidgardItem item) {
        itemMap.put(item.getId(), item);
    }

    /**
     * Remove um item do registro.
     *
     * @param id ID do item a ser removido.
     */
    public void unregisterItem(String id) {
        itemMap.remove(id);
    }

    private void loadItemsFromFolder(File folder) {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                loadItemsFromFolder(file);
            } else if (file.getName().endsWith(".yml")) {
                loadItemsFromFile(file);
            }
        }
    }

    private void loadItemsFromFile(File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        String defaultCategory = "MISCELLANEOUS";
        File itemsFolder = new File(module.getDataFolder(), "item");
        
        if (file.getParentFile().equals(itemsFolder)) {
             defaultCategory = file.getName().replace(".yml", "").toUpperCase();
        } else if (file.getParentFile().getParentFile() != null && file.getParentFile().getParentFile().equals(itemsFolder)) {
             defaultCategory = file.getParentFile().getName().toUpperCase();
        }

        for (String key : config.getKeys(false)) {
            try {
                if (!config.isConfigurationSection(key)) {
                    continue;
                }
                MidgardItem item = new MidgardItemImpl(key, config.getConfigurationSection(key), defaultCategory, file);
                itemMap.put(key, item);
                MidgardLogger.debug(DebugCategory.ITEMS, "Item carregado: %s (Categoria: %s)", key, defaultCategory);
            } catch (Exception e) {
                MidgardLogger.error("Erro ao carregar item " + key + " do arquivo " + file.getName(), e);
            }
        }
    }

    /**
     * Obtém um item pelo ID.
     *
     * @param id ID do item.
     * @return O item correspondente ou null se não encontrado.
     */
    public MidgardItem getMidgardItem(String id) {
        return itemMap.get(id);
    }

    /**
     * Obtém um item pelo ID (alias para getMidgardItem).
     *
     * @param id ID do item.
     * @return O item correspondente ou null se não encontrado.
     */
    public MidgardItem getItem(String id) {
        return getMidgardItem(id);
    }

    /**
     * Obtém o ID do item a partir de um ItemStack.
     *
     * @param item ItemStack a ser verificado.
     * @return O ID do item ou null se não for um item do MidgardRPG.
     */
    public String getItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey idKey = new NamespacedKey(module.getPlugin(), "midgard_id");
        return meta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
    }

    /**
     * Obtém uma lista com todos os IDs de itens registrados.
     *
     * @return Lista de IDs.
     */
    public List<String> getItemIds() {
        return new ArrayList<>(itemMap.keySet());
    }

    public ItemStack getItemStack(String id) {
        MidgardItem item = getMidgardItem(id);
        return item != null ? item.build() : null;
    }
    
    public List<MidgardItem> getItemsByCategory(String categoryId) {
        return itemMap.values().stream()
                .filter(item -> item.getCategoryId().equalsIgnoreCase(categoryId))
                .collect(Collectors.toList());
    }

    public void updateAllOnlinePlayers() {
        // Create a queue of players to update
        final java.util.Queue<org.bukkit.entity.Player> playerQueue = new java.util.LinkedList<>(module.getPlugin().getServer().getOnlinePlayers());
        
        if (playerQueue.isEmpty()) return;

        module.getPlugin().getLogger().info("Iniciando atualização de itens para " + playerQueue.size() + " jogadores...");

        // Schedule a repeating task to process the queue
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                // Process up to 2 players per tick to avoid lag
                for (int i = 0; i < 2; i++) {
                    if (playerQueue.isEmpty()) {
                        module.getPlugin().getLogger().info("Atualização de itens concluída.");
                        this.cancel();
                        return;
                    }

                    org.bukkit.entity.Player player = playerQueue.poll();
                    if (player != null && player.isOnline()) {
                        updateInventory(player);
                    }
                }
            }
        }.runTaskTimer(module.getPlugin(), 1L, 1L);
    }

    public void updateInventory(org.bukkit.entity.Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        boolean updated = false;

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || !item.hasItemMeta()) continue;

            ItemStack newItem = updateItem(item);
            if (newItem != null) {
                contents[i] = newItem;
                updated = true;
            }
        }

        if (updated) {
            player.getInventory().setContents(contents);
            MessageUtils.send(player, MidgardCore.getLanguageManager().getMessage("item.common.updated_to_latest"));
        }
    }

    public ItemStack updateItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        NamespacedKey idKey = new NamespacedKey(module.getPlugin(), "midgard_id");
        NamespacedKey revKey = new NamespacedKey(module.getPlugin(), "midgard_revision");

        String id = meta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
        if (id == null) return null;

        Integer currentRev = meta.getPersistentDataContainer().get(revKey, PersistentDataType.INTEGER);
        if (currentRev == null) currentRev = 1;

        MidgardItem template = getMidgardItem(id);
        if (template == null) return null;

        if (template.getRevisionId() > currentRev) {
            ItemStack newItem = template.build();
            newItem.setAmount(item.getAmount());
            return newItem;
        }

        return null;
    }

    public void saveItem(MidgardItem item) {
        item.save();
    }
}
