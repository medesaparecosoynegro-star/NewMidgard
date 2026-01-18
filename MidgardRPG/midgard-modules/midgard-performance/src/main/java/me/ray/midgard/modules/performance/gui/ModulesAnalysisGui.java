package me.ray.midgard.modules.performance.gui;

import me.ray.midgard.core.gui.BaseGui;
import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.core.utils.ItemBuilder;
import me.ray.midgard.modules.performance.PerformanceModule;
import me.ray.midgard.modules.performance.spark.MidgardAnalyzer;
import me.ray.midgard.modules.performance.spark.MidgardAnalyzer.*;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * GUI para análise detalhada de módulos Midgard.
 * Exibe estatísticas de performance de cada módulo.
 */
public class ModulesAnalysisGui extends BaseGui {

    private static final String TITLE = "<gradient:#a78bfa:#ec4899>📦 Análise de Módulos</gradient>";
    private static final int ITEMS_PER_PAGE = 21;

    private final PerformanceModule module;
    private final List<ModuleAnalysis> modules;
    private int currentPage = 0;

    public ModulesAnalysisGui(Player player, PerformanceModule module) {
        super(player, 6, TITLE);
        this.module = module;
        
        // Carrega módulos ordenados por tempo de inicialização
        var analyzer = MidgardAnalyzer.getInstance();
        if (analyzer != null) {
            var analysis = analyzer.analyze();
            this.modules = new ArrayList<>(analysis.modules());
            this.modules.sort(Comparator.comparingLong(ModuleAnalysis::enableTime).reversed());
        } else {
            this.modules = new ArrayList<>();
        }
    }

    @Override
    public void initializeItems() {
        fillBackground();
        addModuleItems();
        addStatsPanel();
        addNavigationItems();
    }

    private void fillBackground() {
        ItemStack darkPane = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setName(" ")
                .build();
        
        ItemStack accentPane = new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE)
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

    private void addModuleItems() {
        if (modules.isEmpty()) {
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("<gray>Nenhum módulo encontrado.");
            lore.add("");
            lore.add("<dark_gray>O analyzer não detectou módulos");
            lore.add("<dark_gray>ou ainda não foi inicializado.");

            inventory.setItem(22, new ItemBuilder(Material.BARRIER)
                    .setName("<red>Sem Módulos")
                    .lore(parseLore(lore))
                    .build());
            return;
        }

        // Slots disponíveis para módulos (linhas 2-4)
        int[] availableSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        };

        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, modules.size());

        for (int i = startIndex; i < endIndex; i++) {
            int slotIndex = i - startIndex;
            if (slotIndex >= availableSlots.length) break;

            ModuleAnalysis mod = modules.get(i);
            int slot = availableSlots[slotIndex];

            inventory.setItem(slot, createModuleItem(mod));
        }
    }

    private ItemStack createModuleItem(ModuleAnalysis mod) {
        Material material = mod.enabled() ? Material.COMMAND_BLOCK : Material.STRUCTURE_VOID;
        String statusIcon = mod.enabled() ? "<green>✔" : "<red>✘";
        String healthColor = mod.health().getColor();
        String healthIcon = mod.health().getIcon();

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("<gray>Status: " + statusIcon + (mod.enabled() ? " <green>Ativo" : " <red>Desativado"));
        lore.add("<gray>Saúde: " + healthColor + healthIcon + " " + mod.health().name());
        lore.add("");
        lore.add("<dark_gray>═══════════════════");
        lore.add("");
        
        // Tempo de inicialização
        String timeColor = getTimeColor(mod.enableTime());
        lore.add("<gray>⏱ Init: " + timeColor + mod.enableTime() + "ms");
        
        // Operações
        lore.add("<gray>⚙ Operações: <white>" + mod.totalOperations());
        lore.add("<gray>📡 Listeners: <white>" + mod.listenerCount());
        
        // Tempo total de profiling
        if (mod.totalTime() > 0) {
            String profileColor = getTimeColor(mod.totalTime());
            lore.add("<gray>📊 Tempo profiled: " + profileColor + mod.totalTime() + "ms");
        }

        // Top 3 operações lentas
        if (!mod.operations().isEmpty()) {
            lore.add("");
            lore.add("<yellow>Operações mais lentas:");
            
            var slowOps = mod.operations().stream()
                    .sorted(Comparator.comparingLong(OperationAnalysis::maxTime).reversed())
                    .limit(3)
                    .toList();
            
            for (var op : slowOps) {
                String opColor = op.severity().getColor();
                String opIcon = op.severity().getIcon();
                String shortName = shortenOperationName(op.name());
                lore.add("  " + opColor + opIcon + " <gray>" + shortName + ": <white>" + op.maxTime() + "ms");
            }
        }

        return new ItemBuilder(material)
                .setName(healthColor + mod.name())
                .lore(parseLore(lore))
                .glowIf(mod.health().ordinal() >= me.ray.midgard.modules.performance.spark.SparkPerformanceManager.HealthLevel.WARNING.ordinal())
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build();
    }

    private void addStatsPanel() {
        var analyzer = MidgardAnalyzer.getInstance();
        if (analyzer == null) return;

        var analysis = analyzer.analyze();

        // Summary stats in slot 4
        int totalModules = modules.size();
        long enabledModules = modules.stream().filter(ModuleAnalysis::enabled).count();
        long totalOps = analysis.profiler().totalExecutions();
        long totalTime = analysis.profiler().totalTime();

        List<String> summaryLore = new ArrayList<>();
        summaryLore.add("");
        summaryLore.add("<gray>Resumo da análise de módulos");
        summaryLore.add("");
        summaryLore.add("<dark_gray>▸ Módulos ativos: <green>" + enabledModules + "<gray>/" + totalModules);
        summaryLore.add("<dark_gray>▸ Total operações: <white>" + totalOps);
        summaryLore.add("<dark_gray>▸ Tempo total: <white>" + totalTime + "ms");
        summaryLore.add("<dark_gray>▸ Listeners: <white>" + analysis.events().totalListeners());
        summaryLore.add("");
        
        // Warnings
        long slowModules = modules.stream()
                .filter(m -> m.enableTime() > 500)
                .count();
        
        if (slowModules > 0) {
            summaryLore.add("<yellow>⚠ " + slowModules + " módulo(s) com init lento");
        }
        
        long criticalOps = modules.stream()
                .flatMap(m -> m.operations().stream())
                .filter(op -> op.severity() == Severity.CRITICAL || op.severity() == Severity.SEVERE)
                .count();
        
        if (criticalOps > 0) {
            summaryLore.add("<red>⚠ " + criticalOps + " operação(ões) crítica(s)");
        }

        inventory.setItem(4, new ItemBuilder(Material.ENCHANTED_BOOK)
                .setName("<gradient:#a78bfa:#ec4899>📊 Resumo</gradient>")
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
        int totalPages = Math.max(1, (int) Math.ceil(modules.size() / (double) ITEMS_PER_PAGE));
        List<String> pageLore = new ArrayList<>();
        pageLore.add("");
        pageLore.add("<gray>Página " + (currentPage + 1) + " de " + totalPages);
        pageLore.add("");
        pageLore.add("<dark_gray>Total: " + modules.size() + " módulos");

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
        if ((currentPage + 1) * ITEMS_PER_PAGE < modules.size()) {
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
                .addLore("<gray>Atualizar dados")
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
                if ((currentPage + 1) * ITEMS_PER_PAGE < modules.size()) {
                    clicker.playSound(clicker.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                    currentPage++;
                    initializeItems();
                }
            }
            case 53 -> { // Refresh
                clicker.playSound(clicker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
                // Recarrega dados
                var analyzer = MidgardAnalyzer.getInstance();
                if (analyzer != null) {
                    var analysis = analyzer.analyze();
                    modules.clear();
                    modules.addAll(analysis.modules());
                    modules.sort(Comparator.comparingLong(ModuleAnalysis::enableTime).reversed());
                }
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

    private String getTimeColor(long ms) {
        if (ms < 100) return "<green>";
        if (ms < 300) return "<yellow>";
        if (ms < 500) return "<gold>";
        return "<red>";
    }

    private String shortenOperationName(String name) {
        // Encurta nomes longos de operações
        if (name.length() <= 25) return name;
        
        // Tenta extrair a parte mais relevante
        if (name.contains(":")) {
            String[] parts = name.split(":");
            return parts[parts.length - 1];
        }
        
        return name.substring(0, 22) + "...";
    }
}
