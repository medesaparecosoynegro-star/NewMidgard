package me.ray.midgard.core.gui;

import me.ray.midgard.core.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utilitários para criação e manipulação de GUIs.
 * Fornece métodos helper para criar elementos visuais padronizados.
 */
public class GuiUtils {
    
    /**
     * Cria um indicador de estado visual padrão.
     * 
     * @param state Estado visual a ser representado
     * @param name Nome do item
     * @param lore Lore do item
     * @return ItemStack configurado com o estado visual
     */
    public static ItemStack createStateIndicator(VisualState state, String name, String... lore) {
        ItemBuilder builder = new ItemBuilder(state.getMaterial())
                .setName(state.format(name));
        
        for (String line : lore) {
            builder.addLore(line);
        }
        
        return builder.build();
    }
    
    /**
     * Cria um indicador de estado com efeito glow.
     * 
     * @param state Estado visual
     * @param name Nome do item
     * @param glowing Se deve ter efeito glow
     * @param lore Lore do item
     * @return ItemStack com estado e glow
     */
    public static ItemStack createStateIndicator(VisualState state, String name, boolean glowing, String... lore) {
        ItemBuilder builder = new ItemBuilder(state.getMaterial())
                .setName(state.format(name));
        
        for (String line : lore) {
            builder.addLore(line);
        }
        
        if (glowing) {
            builder.glow();
        }
        
        return builder.build();
    }
    
    /**
     * Formata uma seção de lore com título e conteúdo.
     * 
     * @param title Título da seção
     * @param content Linhas de conteúdo
     * @return Lista formatada
     */
    public static List<String> formatLoreSection(String title, List<String> content) {
        List<String> formatted = new ArrayList<>();
        formatted.add("");
        formatted.add("§7▸ §f" + title + ":");
        for (String line : content) {
            formatted.add("  §7" + line);
        }
        return formatted;
    }
    
    /**
     * Formata uma seção de lore com título e conteúdo (varargs).
     */
    public static List<String> formatLoreSection(String title, String... content) {
        return formatLoreSection(title, Arrays.asList(content));
    }
    
    /**
     * Cria uma lore estruturada completa com múltiplas seções.
     * 
     * @param description Descrição principal
     * @param howToUse Como usar (passo-a-passo)
     * @param tips Dicas úteis
     * @param action Ação do clique
     * @return Lista de lore formatada
     */
    public static List<String> createStructuredLore(String description, List<String> howToUse, String tips, String action) {
        List<String> lore = new ArrayList<>();
        
        // Descrição
        if (description != null && !description.isEmpty()) {
            lore.add("");
            lore.add("§7▸ §fDescrição:");
            lore.add("  §7" + description);
        }
        
        // Como usar
        if (howToUse != null && !howToUse.isEmpty()) {
            lore.add("");
            lore.add("§7▸ §fComo usar:");
            int step = 1;
            for (String line : howToUse) {
                lore.add("  §7" + step + ". " + line);
                step++;
            }
        }
        
        // Dicas
        if (tips != null && !tips.isEmpty()) {
            lore.add("");
            lore.add("§7▸ §fDica:");
            lore.add("  §e💡 " + tips);
        }
        
        // Ação
        if (action != null && !action.isEmpty()) {
            lore.add("");
            lore.add("§a▸ " + action);
        }
        
        return lore;
    }
    
    /**
     * Cria um botão de navegação "Voltar".
     */
    public static ItemStack createBackButton() {
        return new ItemBuilder(Material.ARROW)
                .setName("§c§l← Voltar")
                .addLore("§7Clique para voltar ao menu anterior")
                .build();
    }
    
    /**
     * Cria um botão de navegação "Próximo".
     */
    public static ItemStack createNextButton() {
        return new ItemBuilder(Material.ARROW)
                .setName("§a§l→ Próximo")
                .addLore("§7Clique para ir para a próxima página")
                .build();
    }
    
    /**
     * Cria um botão de navegação "Anterior".
     */
    public static ItemStack createPreviousButton() {
        return new ItemBuilder(Material.ARROW)
                .setName("§e§l← Anterior")
                .addLore("§7Clique para voltar à página anterior")
                .build();
    }
    
    /**
     * Cria um botão de ajuda.
     */
    public static ItemStack createHelpButton() {
        return new ItemBuilder(Material.BOOK)
                .setName("§b§l❓ Ajuda")
                .addLore("")
                .addLore("§7Clique para abrir o menu de ajuda")
                .addLore("§7e aprender a usar este sistema.")
                .glow()
                .build();
    }
    
    /**
     * Cria um botão de confirmação.
     */
    public static ItemStack createConfirmButton(String action) {
        return new ItemBuilder(Material.LIME_DYE)
                .setName("§a§l✓ Confirmar")
                .addLore("§7Clique para " + action)
                .build();
    }
    
    /**
     * Cria um botão de cancelamento.
     */
    public static ItemStack createCancelButton() {
        return new ItemBuilder(Material.RED_DYE)
                .setName("§c§l✗ Cancelar")
                .addLore("§7Clique para cancelar esta ação")
                .build();
    }
    
    /**
     * Cria um item de informação dinâmica.
     * 
     * @param title Título da informação
     * @param lines Linhas de informação
     * @return ItemStack de informação
     */
    public static ItemStack createInfoItem(String title, String... lines) {
        ItemBuilder builder = new ItemBuilder(Material.PAPER)
                .setName("§e§l⚡ " + title);
        
        for (String line : lines) {
            builder.addLore("§7" + line);
        }
        
        return builder.build();
    }
    
    /**
     * Cria um separador visual (vidro colorido).
     */
    public static ItemStack createSeparator(Material glassPane) {
        return new ItemBuilder(glassPane)
                .setName(" ")
                .build();
    }
    
    /**
     * Cria um item de preenchimento (filler).
     */
    public static ItemStack createFiller() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setName(" ")
                .build();
    }
}
