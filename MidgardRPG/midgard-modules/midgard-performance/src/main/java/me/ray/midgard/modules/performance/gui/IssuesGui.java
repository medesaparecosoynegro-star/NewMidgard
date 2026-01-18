package me.ray.midgard.modules.performance.gui;

import me.ray.midgard.core.gui.BaseGui;
import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.core.utils.ItemBuilder;
import me.ray.midgard.modules.performance.PerformanceModule;
import me.ray.midgard.modules.performance.spark.PerformanceReport;
import me.ray.midgard.modules.performance.spark.PerformanceReport.*;
import me.ray.midgard.modules.performance.spark.SparkPerformanceManager.HealthLevel;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI para visualização de problemas detectados.
 * Lista issues ordenados por severidade.
 */
public class IssuesGui extends BaseGui {

    private static final String TITLE = "<gradient:#ef4444:#f97316>⚠ Problemas Detectados</gradient>";
    private static final int ITEMS_PER_PAGE = 21;

    private final PerformanceModule module;
    private final List<Issue> issues;
    private int currentPage = 0;

    public IssuesGui(Player player, PerformanceModule module) {
        super(player, 6, TITLE);
        this.module = module;
        
        // Carrega issues ordenados por severidade
        var report = PerformanceReport.generateFullReport();
        this.issues = new ArrayList<>(report.issues());
    }

    @Override
    public void initializeItems() {
        fillBackground();
        addIssueItems();
        addSummaryPanel();
        addNavigationItems();
    }

    private void fillBackground() {
        ItemStack darkPane = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setName(" ")
                .build();
        
        ItemStack accentPane = new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .setName(" ")
                .build();

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, darkPane);
        }

        // Top accent
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, accentPane);
        }

        // Bottom accent
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, accentPane);
        }
    }

    private void addIssueItems() {
        if (issues.isEmpty()) {
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("<green>Nenhum problema detectado!");
            lore.add("");
            lore.add("<gray>O servidor está funcionando");
            lore.add("<gray>dentro dos parâmetros normais.");
            lore.add("");
            lore.add("<dark_gray>Continue monitorando para");
            lore.add("<dark_gray>identificar problemas futuros.");

            inventory.setItem(22, new ItemBuilder(Material.EMERALD)
                    .setName("<green>✔ Tudo OK!")
                    .lore(parseLore(lore))
                    .glow()
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .build());
            return;
        }

        // Slots disponíveis para issues
        int[] availableSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        };

        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, issues.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slotIndex = i - startIndex;
            if (slotIndex >= availableSlots.length) break;

            Issue issue = issues.get(i);
            int slot = availableSlots[slotIndex];

            inventory.setItem(slot, createIssueItem(issue));
        }
    }

    private ItemStack createIssueItem(Issue issue) {
        Material material = getIssueMaterial(issue.level());
        String levelColor = issue.level().getColor();
        String categoryIcon = issue.category().getIcon();

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(levelColor + "Severidade: " + issue.level().getLabel());
        lore.add("<gray>Categoria: <white>" + issue.category().name());
        lore.add("");
        lore.add("<dark_gray>═══════════════════");
        lore.add("");
        
        // Descrição (quebra em linhas se necessário)
        String description = issue.description();
        if (description.length() > 40) {
            for (String line : wrapText(description, 35)) {
                lore.add("<gray>" + line);
            }
        } else {
            lore.add("<gray>" + description);
        }
        
        lore.add("");
        lore.add("<yellow>💡 Sugestão:");
        
        // Sugestão (quebra em linhas)
        String suggestion = issue.suggestion();
        if (suggestion.length() > 38) {
            for (String line : wrapText(suggestion, 33)) {
                lore.add("<dark_gray>  " + line);
            }
        } else {
            lore.add("<dark_gray>  " + suggestion);
        }

        return new ItemBuilder(material)
                .setName(levelColor + categoryIcon + " " + issue.title())
                .lore(parseLore(lore))
                .glowIf(issue.level().ordinal() >= HealthLevel.CRITICAL.ordinal())
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build();
    }

    private void addSummaryPanel() {
        // Count by severity
        long severeCount = issues.stream().filter(i -> i.level() == HealthLevel.SEVERE).count();
        long criticalCount = issues.stream().filter(i -> i.level() == HealthLevel.CRITICAL).count();
        long warningCount = issues.stream().filter(i -> i.level() == HealthLevel.WARNING).count();
        long goodCount = issues.stream().filter(i -> i.level() == HealthLevel.GOOD).count();

        List<String> summaryLore = new ArrayList<>();
        summaryLore.add("");
        summaryLore.add("<gray>Resumo de problemas detectados");
        summaryLore.add("");
        summaryLore.add("<dark_gray>Total: <white>" + issues.size() + " problema(s)");
        summaryLore.add("");
        
        if (severeCount > 0) {
            summaryLore.add("<dark_red>⚠ Severos: " + severeCount);
        }
        if (criticalCount > 0) {
            summaryLore.add("<red>⚠ Críticos: " + criticalCount);
        }
        if (warningCount > 0) {
            summaryLore.add("<yellow>⚠ Avisos: " + warningCount);
        }
        if (goodCount > 0) {
            summaryLore.add("<green>ℹ Informações: " + goodCount);
        }
        
        if (issues.isEmpty()) {
            summaryLore.add("<green>✔ Nenhum problema!");
        }
        
        summaryLore.add("");
        summaryLore.add("<dark_gray>Problemas são detectados");
        summaryLore.add("<dark_gray>automaticamente com base nas");
        summaryLore.add("<dark_gray>métricas de performance.");

        Material summaryMat = severeCount > 0 ? Material.TNT : 
                              criticalCount > 0 ? Material.REDSTONE_BLOCK :
                              warningCount > 0 ? Material.GOLD_BLOCK : Material.EMERALD;

        inventory.setItem(4, new ItemBuilder(summaryMat)
                .setName("<gradient:#ef4444:#f97316>📊 Resumo</gradient>")
                .lore(parseLore(summaryLore))
                .glow()
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());
    }

    private void addNavigationItems() {
        // Back button
        inventory.setItem(45, new ItemBuilder(Material.ARROW)
                .setName("<yellow>← Voltar")
                .addLore("")
                .addLore("<gray>Voltar ao Dashboard")
                .build());

        // Page info
        int totalPages = Math.max(1, (int) Math.ceil(issues.size() / (double) ITEMS_PER_PAGE));
        List<String> pageLore = new ArrayList<>();
        pageLore.add("");
        pageLore.add("<gray>Página " + (currentPage + 1) + " de " + totalPages);
        pageLore.add("");
        pageLore.add("<dark_gray>Total: " + issues.size() + " problemas");

        inventory.setItem(49, new ItemBuilder(Material.PAPER)
                .setName("<white>Página " + (currentPage + 1) + "/" + totalPages)
                .lore(parseLore(pageLore))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        // Previous page
        if (currentPage > 0) {
            inventory.setItem(48, new ItemBuilder(Material.SPECTRAL_ARROW)
                    .setName("<yellow>← Anterior")
                    .addLore("")
                    .addLore("<gray>Ir para página " + currentPage)
                    .build());
        }

        // Next page
        if ((currentPage + 1) * ITEMS_PER_PAGE < issues.size()) {
            inventory.setItem(50, new ItemBuilder(Material.SPECTRAL_ARROW)
                    .setName("<yellow>Próxima →")
                    .addLore("")
                    .addLore("<gray>Ir para página " + (currentPage + 2))
                    .build());
        }

        // Refresh
        inventory.setItem(53, new ItemBuilder(Material.SUNFLOWER)
                .setName("<gradient:#fbbf24:#f59e0b>🔄 Atualizar</gradient>")
                .addLore("")
                .addLore("<gray>Atualizar análise")
                .build());
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        if (!clicker.equals(this.player)) return;

        int slot = event.getRawSlot();

        switch (slot) {
            case 45 -> { // Back
                clicker.playSound(clicker.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                new PerformanceMainGui(clicker, module).open();
            }
            case 48 -> { // Previous page
                if (currentPage > 0) {
                    clicker.playSound(clicker.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                    currentPage--;
                    initializeItems();
                }
            }
            case 50 -> { // Next page
                if ((currentPage + 1) * ITEMS_PER_PAGE < issues.size()) {
                    clicker.playSound(clicker.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                    currentPage++;
                    initializeItems();
                }
            }
            case 53 -> { // Refresh
                clicker.playSound(clicker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
                // Recarrega issues
                var report = PerformanceReport.generateFullReport();
                issues.clear();
                issues.addAll(report.issues());
                currentPage = 0;
                initializeItems();
            }
        }
    }

    // ===== HELPER METHODS =====

    private List<net.kyori.adventure.text.Component> parseLore(List<String> lore) {
        List<net.kyori.adventure.text.Component> components = new ArrayList<>();
        for (String line : lore) {
            components.add(MessageUtils.parse(line));
        }
        return components;
    }

    private Material getIssueMaterial(HealthLevel level) {
        return switch (level) {
            case SEVERE -> Material.TNT;
            case CRITICAL -> Material.REDSTONE;
            case WARNING -> Material.GOLD_INGOT;
            case GOOD -> Material.LIME_DYE;
            case EXCELLENT -> Material.EMERALD;
            default -> Material.GRAY_DYE;
        };
    }

    private List<String> wrapText(String text, int maxLength) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxLength) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString().trim());
                    currentLine = new StringBuilder();
                }
            }
            currentLine.append(word).append(" ");
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString().trim());
        }

        return lines;
    }
}
