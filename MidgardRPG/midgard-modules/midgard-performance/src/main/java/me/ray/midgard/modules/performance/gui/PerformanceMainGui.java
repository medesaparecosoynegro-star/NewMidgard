package me.ray.midgard.modules.performance.gui;

import me.ray.midgard.core.gui.BaseGui;
import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.core.utils.ItemBuilder;
import me.ray.midgard.modules.performance.PerformanceModule;
import me.ray.midgard.modules.performance.spark.MidgardAnalyzer;
import me.ray.midgard.modules.performance.spark.PerformanceReport;
import me.ray.midgard.modules.performance.spark.SparkPerformanceManager;
import me.ray.midgard.modules.performance.spark.SparkPerformanceManager.*;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard principal do módulo de Performance.
 * Exibe visão geral e navegação para submenus detalhados.
 */
public class PerformanceMainGui extends BaseGui {

    private static final String TITLE = "<gradient:#ff6b6b:#feca57>⚡ Performance Dashboard</gradient>";
    
    private final PerformanceModule module;
    private int refreshTaskId = -1;

    public PerformanceMainGui(Player player, PerformanceModule module) {
        super(player, 6, TITLE);
        this.module = module;
    }

    @Override
    public void initializeItems() {
        fillBackground();
        updateMetrics();
        addNavigationItems();
        addInfoItems();
    }

    private void fillBackground() {
        ItemStack darkPane = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setName(" ")
                .build();
        
        ItemStack accentPane = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setName(" ")
                .build();

        // Preenche tudo com preto
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, darkPane);
        }

        // Bordas com cinza
        int[] borderSlots = {0, 1, 7, 8, 9, 17, 36, 44, 45, 46, 52, 53};
        for (int slot : borderSlots) {
            inventory.setItem(slot, accentPane);
        }
    }

    private void updateMetrics() {
        if (!SparkPerformanceManager.isAvailable()) {
            addSparkWarning();
            return;
        }

        var manager = SparkPerformanceManager.getInstance();
        var metrics = manager.getMetrics();
        var diagnosis = manager.diagnose();

        // ===== HEALTH STATUS (Slot 4 - Topo central) =====
        HealthLevel health = diagnosis.overallHealth();
        Material healthMaterial = getHealthMaterial(health);
        
        List<String> healthLore = new ArrayList<>();
        healthLore.add("");
        healthLore.add("<gray>Status atual do servidor");
        healthLore.add("");
        healthLore.add(health.getColor() + health.getIcon() + " " + health.getLabel());
        healthLore.add("");
        healthLore.add("<dark_gray>▸ TPS: " + diagnosis.tps().getColor() + diagnosis.tps().message());
        healthLore.add("<dark_gray>▸ Memory: " + diagnosis.memory().getColor() + diagnosis.memory().message());
        healthLore.add("<dark_gray>▸ CPU: " + diagnosis.cpu().getColor() + diagnosis.cpu().message());
        healthLore.add("");
        healthLore.add("<yellow>Clique para ver diagnóstico completo");

        inventory.setItem(4, new ItemBuilder(healthMaterial)
                .setName(health.getColor() + "✦ Saúde do Servidor")
                .lore(parseLore(healthLore))
                .glow()
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        // ===== TPS GAUGE (Slot 20) =====
        var tps = metrics.tps();
        double tpsValue = tps.available() ? tps.last5s() : 0;
        String tpsColor = getTpsColor(tpsValue);
        Material tpsMaterial = getTpsMaterial(tpsValue);

        List<String> tpsLore = new ArrayList<>();
        tpsLore.add("");
        if (tps.available()) {
            tpsLore.add("<gray>Ticks por segundo");
            tpsLore.add("");
            tpsLore.add(tpsColor + "⏱ " + String.format("%.1f", tpsValue) + " <gray>TPS");
            tpsLore.add("");
            tpsLore.add("<dark_gray>5s:  " + getTpsColor(tps.last5s()) + String.format("%.2f", tps.last5s()));
            tpsLore.add("<dark_gray>1m:  " + getTpsColor(tps.last1m()) + String.format("%.2f", tps.last1m()));
            tpsLore.add("<dark_gray>5m:  " + getTpsColor(tps.last5m()) + String.format("%.2f", tps.last5m()));
            tpsLore.add("");
            tpsLore.add(createProgressBar(tpsValue / 20.0, tpsColor));
        } else {
            tpsLore.add("<red>TPS indisponível");
        }
        tpsLore.add("");
        tpsLore.add("<yellow>Clique para detalhes");

        inventory.setItem(20, new ItemBuilder(tpsMaterial)
                .setName("<gradient:#ff6b6b:#feca57>⏱ TPS Monitor</gradient>")
                .lore(parseLore(tpsLore))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        // ===== MSPT GAUGE (Slot 21) =====
        var mspt = metrics.mspt();
        double msptValue = mspt.available() ? mspt.last10s().median() : 0;
        String msptColor = getMsptColor(msptValue);

        List<String> msptLore = new ArrayList<>();
        msptLore.add("");
        if (mspt.available()) {
            msptLore.add("<gray>Milliseconds por tick");
            msptLore.add("");
            msptLore.add(msptColor + "⚡ " + String.format("%.1f", msptValue) + "ms <gray>mediana");
            msptLore.add("");
            var w = mspt.last10s();
            msptLore.add("<dark_gray>Min: " + getMsptColor(w.min()) + String.format("%.1f", w.min()) + "ms");
            msptLore.add("<dark_gray>P95: " + getMsptColor(w.p95()) + String.format("%.1f", w.p95()) + "ms");
            msptLore.add("<dark_gray>Max: " + getMsptColor(w.max()) + String.format("%.1f", w.max()) + "ms");
            msptLore.add("");
            msptLore.add(createProgressBar(1 - (msptValue / 50.0), msptColor));
        } else {
            msptLore.add("<red>MSPT indisponível");
        }
        msptLore.add("");
        msptLore.add("<yellow>Clique para detalhes");

        inventory.setItem(21, new ItemBuilder(Material.CLOCK)
                .setName("<gradient:#feca57:#ff6b6b>⚡ MSPT Monitor</gradient>")
                .lore(parseLore(msptLore))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        // ===== MEMORY GAUGE (Slot 23) =====
        var mem = metrics.memory();
        double memPercent = mem.available() ? mem.usedPercent() : 0;
        String memColor = getMemoryColor(memPercent);

        List<String> memLore = new ArrayList<>();
        memLore.add("");
        if (mem.available()) {
            memLore.add("<gray>Uso de memória heap");
            memLore.add("");
            memLore.add(memColor + "💾 " + mem.usedMB() + "MB <gray>/ " + mem.maxMB() + "MB");
            memLore.add("");
            memLore.add("<dark_gray>Usado: " + memColor + String.format("%.1f%%", memPercent));
            memLore.add("<dark_gray>Livre: <green>" + mem.freeMB() + "MB");
            memLore.add("<dark_gray>Alocado: <aqua>" + mem.totalMB() + "MB");
            memLore.add("");
            memLore.add(createProgressBar(memPercent / 100.0, memColor));
        } else {
            memLore.add("<red>Memória indisponível");
        }
        memLore.add("");
        memLore.add("<yellow>Clique para detalhes");

        Material memMaterial = memPercent > 85 ? Material.REDSTONE_BLOCK : 
                               memPercent > 70 ? Material.GOLD_BLOCK : Material.EMERALD_BLOCK;

        inventory.setItem(23, new ItemBuilder(memMaterial)
                .setName("<gradient:#4ade80:#22d3ee>💾 Memory Monitor</gradient>")
                .lore(parseLore(memLore))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        // ===== CPU GAUGE (Slot 24) =====
        var cpu = metrics.cpu();
        double cpuPercent = cpu.available() ? cpu.process().seconds10() * 100 : 0;
        String cpuColor = getCpuColor(cpuPercent);

        List<String> cpuLore = new ArrayList<>();
        cpuLore.add("");
        if (cpu.available()) {
            cpuLore.add("<gray>Uso de CPU do processo");
            cpuLore.add("");
            cpuLore.add(cpuColor + "💻 " + String.format("%.1f%%", cpuPercent) + " <gray>processo");
            cpuLore.add("");
            var p = cpu.process();
            cpuLore.add("<dark_gray>10s: " + getCpuColor(p.seconds10() * 100) + p.formatPercent(p.seconds10()));
            cpuLore.add("<dark_gray>1m:  " + getCpuColor(p.minutes1() * 100) + p.formatPercent(p.minutes1()));
            cpuLore.add("<dark_gray>15m: " + getCpuColor(p.minutes15() * 100) + p.formatPercent(p.minutes15()));
            cpuLore.add("");
            cpuLore.add(createProgressBar(cpuPercent / 100.0, cpuColor));
        } else {
            cpuLore.add("<red>CPU indisponível");
        }
        cpuLore.add("");
        cpuLore.add("<yellow>Clique para detalhes");

        inventory.setItem(24, new ItemBuilder(Material.REDSTONE_TORCH)
                .setName("<gradient:#f472b6:#a78bfa>💻 CPU Monitor</gradient>")
                .lore(parseLore(cpuLore))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());
    }

    private void addNavigationItems() {
        // ===== MÓDULOS (Slot 29) =====
        List<String> modulesLore = new ArrayList<>();
        modulesLore.add("");
        modulesLore.add("<gray>Análise de módulos Midgard");
        modulesLore.add("");
        
        var analyzer = MidgardAnalyzer.getInstance();
        if (analyzer != null) {
            var analysis = analyzer.analyze();
            int total = analysis.modules().size();
            long enabled = analysis.modules().stream().filter(m -> m.enabled()).count();
            modulesLore.add("<dark_gray>▸ Ativos: <green>" + enabled + "<gray>/" + total);
            modulesLore.add("<dark_gray>▸ Total operações: <white>" + analysis.profiler().totalExecutions());
        } else {
            modulesLore.add("<red>Analyzer não disponível");
        }
        modulesLore.add("");
        modulesLore.add("<yellow>Clique para ver módulos");

        inventory.setItem(29, new ItemBuilder(Material.COMMAND_BLOCK)
                .setName("<gradient:#a78bfa:#ec4899>📦 Módulos Midgard</gradient>")
                .lore(parseLore(modulesLore))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        // ===== ISSUES (Slot 31) =====
        var report = PerformanceReport.generateQuickReport();
        int criticalIssues = report.criticalIssues();
        Material issueMaterial = criticalIssues > 0 ? Material.TNT : Material.EMERALD;

        List<String> issuesLore = new ArrayList<>();
        issuesLore.add("");
        issuesLore.add("<gray>Problemas detectados");
        issuesLore.add("");
        if (criticalIssues > 0) {
            issuesLore.add("<red>⚠ " + criticalIssues + " problemas críticos!");
        } else {
            issuesLore.add("<green>✔ Nenhum problema crítico");
        }
        issuesLore.add("");
        issuesLore.add("<yellow>Clique para ver detalhes");

        inventory.setItem(31, new ItemBuilder(issueMaterial)
                .setName("<gradient:#ef4444:#f97316>⚠ Problemas</gradient>")
                .lore(parseLore(issuesLore))
                .glowIf(criticalIssues > 0)
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        // ===== RELATÓRIO (Slot 33) =====
        var fullReport = PerformanceReport.generateFullReport();
        int score = fullReport.overallScore();
        String scoreColor = getScoreColor(score);

        List<String> reportLore = new ArrayList<>();
        reportLore.add("");
        reportLore.add("<gray>Relatório completo de performance");
        reportLore.add("");
        reportLore.add("<dark_gray>Score: " + scoreColor + score + "/100 <dark_gray>[" + scoreColor + fullReport.getScoreGrade() + "<dark_gray>]");
        reportLore.add("");
        reportLore.add("<dark_gray>▸ Issues: <yellow>" + fullReport.issues().size());
        reportLore.add("<dark_gray>▸ Recomendações: <aqua>" + fullReport.recommendations().size());
        reportLore.add("");
        reportLore.add("<yellow>Clique para relatório completo");

        inventory.setItem(33, new ItemBuilder(Material.BOOK)
                .setName("<gradient:#fbbf24:#f59e0b>📊 Relatório</gradient>")
                .lore(parseLore(reportLore))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());
    }

    private void addInfoItems() {
        // ===== GC INFO (Slot 40) =====
        if (SparkPerformanceManager.isAvailable()) {
            var gc = SparkPerformanceManager.getInstance().getMetrics().gc();
            
            List<String> gcLore = new ArrayList<>();
            gcLore.add("");
            if (gc.available()) {
                gcLore.add("<gray>Garbage Collection");
                gcLore.add("");
                gcLore.add("<dark_gray>Coletas: <white>" + gc.totalCollections());
                gcLore.add("<dark_gray>Tempo total: <white>" + gc.formatTime());
                gcLore.add("<dark_gray>Média: <white>" + String.format("%.2f", gc.avgTime()) + "ms");
            } else {
                gcLore.add("<red>GC indisponível");
            }
            gcLore.add("");
            gcLore.add("<yellow>Clique para detalhes");

            inventory.setItem(40, new ItemBuilder(Material.HOPPER)
                    .setName("<gradient:#94a3b8:#64748b>🗑 Garbage Collector</gradient>")
                    .lore(parseLore(gcLore))
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .build());
        }

        // ===== WATCHER STATUS (Slot 49) =====
        var watcher = module.getHealthWatcher();
        List<String> watcherLore = new ArrayList<>();
        watcherLore.add("");
        if (watcher != null) {
            watcherLore.add("<gray>Monitoramento contínuo");
            watcherLore.add("");
            watcherLore.add("<dark_gray>Checks: <white>" + watcher.getChecksPerformed());
            watcherLore.add("<dark_gray>Alertas: <yellow>" + watcher.getAlertsTriggered());
            watcherLore.add("");
            watcherLore.add("<dark_gray>TPS: " + (watcher.isTpsAlertActive() ? "<red>⚠ ALERTA" : "<green>✔ OK"));
            watcherLore.add("<dark_gray>RAM: " + (watcher.isMemoryAlertActive() ? "<red>⚠ ALERTA" : "<green>✔ OK"));
            watcherLore.add("<dark_gray>CPU: " + (watcher.isCpuAlertActive() ? "<red>⚠ ALERTA" : "<green>✔ OK"));
        } else {
            watcherLore.add("<red>Watcher não inicializado");
        }

        inventory.setItem(49, new ItemBuilder(Material.ENDER_EYE)
                .setName("<gradient:#22d3ee:#3b82f6>👁 Health Watcher</gradient>")
                .lore(parseLore(watcherLore))
                .glowIf(watcher != null && (watcher.isTpsAlertActive() || watcher.isMemoryAlertActive() || watcher.isCpuAlertActive()))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        // ===== REFRESH (Slot 53) =====
        List<String> refreshLore = new ArrayList<>();
        refreshLore.add("");
        refreshLore.add("<gray>Atualizar métricas");
        refreshLore.add("");
        refreshLore.add("<dark_gray>Auto-refresh: <yellow>desativado");
        refreshLore.add("");
        refreshLore.add("<yellow>Clique para atualizar");

        inventory.setItem(53, new ItemBuilder(Material.SUNFLOWER)
                .setName("<gradient:#fbbf24:#f59e0b>🔄 Atualizar</gradient>")
                .lore(parseLore(refreshLore))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        // ===== FECHAR (Slot 45) =====
        inventory.setItem(45, new ItemBuilder(Material.BARRIER)
                .setName("<red>✖ Fechar")
                .addLore("")
                .addLore("<gray>Clique para fechar")
                .build());
    }

    private void addSparkWarning() {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("<red>Spark Profiler não detectado!");
        lore.add("");
        lore.add("<gray>O módulo de Performance requer");
        lore.add("<gray>o Spark para métricas precisas.");
        lore.add("");
        lore.add("<aqua>Download: spark.lucko.me");
        lore.add("");

        inventory.setItem(22, new ItemBuilder(Material.BARRIER)
                .setName("<red>⚠ Spark Necessário</red>")
                .lore(parseLore(lore))
                .glow()
                .build());
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        if (!clicker.equals(this.player)) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= inventory.getSize()) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        switch (slot) {
            case 4 -> { // Health Status -> Diagnóstico
                clicker.playSound(clicker.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                new MetricsDetailGui(clicker, module, MetricsDetailGui.MetricType.DIAGNOSE).open();
            }
            case 20 -> { // TPS
                clicker.playSound(clicker.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                new MetricsDetailGui(clicker, module, MetricsDetailGui.MetricType.TPS).open();
            }
            case 21 -> { // MSPT
                clicker.playSound(clicker.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                new MetricsDetailGui(clicker, module, MetricsDetailGui.MetricType.MSPT).open();
            }
            case 23 -> { // Memory
                clicker.playSound(clicker.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                new MetricsDetailGui(clicker, module, MetricsDetailGui.MetricType.MEMORY).open();
            }
            case 24 -> { // CPU
                clicker.playSound(clicker.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                new MetricsDetailGui(clicker, module, MetricsDetailGui.MetricType.CPU).open();
            }
            case 29 -> { // Módulos
                clicker.playSound(clicker.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                new ModulesAnalysisGui(clicker, module).open();
            }
            case 31 -> { // Issues
                clicker.playSound(clicker.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                new IssuesGui(clicker, module).open();
            }
            case 33 -> { // Relatório
                clicker.playSound(clicker.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                new ReportGui(clicker, module).open();
            }
            case 40 -> { // GC
                clicker.playSound(clicker.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                new MetricsDetailGui(clicker, module, MetricsDetailGui.MetricType.GC).open();
            }
            case 45 -> { // Fechar
                clicker.playSound(clicker.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.5f, 1.0f);
                clicker.closeInventory();
            }
            case 53 -> { // Refresh
                clicker.playSound(clicker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
                updateMetrics();
                addNavigationItems();
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

    private String createProgressBar(double percent, String color) {
        percent = Math.max(0, Math.min(1, percent));
        int filled = (int) (percent * 20);
        StringBuilder bar = new StringBuilder("<dark_gray>[");
        for (int i = 0; i < 20; i++) {
            if (i < filled) {
                bar.append(color).append("█");
            } else {
                bar.append("<dark_gray>░");
            }
        }
        bar.append("<dark_gray>]");
        return bar.toString();
    }

    private Material getHealthMaterial(HealthLevel health) {
        return switch (health) {
            case EXCELLENT -> Material.EMERALD;
            case GOOD -> Material.LIME_DYE;
            case WARNING -> Material.GOLD_INGOT;
            case CRITICAL -> Material.REDSTONE;
            case SEVERE -> Material.NETHER_STAR;
            default -> Material.GRAY_DYE;
        };
    }

    private String getTpsColor(double tps) {
        if (tps >= 19) return "<green>";
        if (tps >= 17) return "<yellow>";
        if (tps >= 15) return "<gold>";
        return "<red>";
    }

    private Material getTpsMaterial(double tps) {
        if (tps >= 19) return Material.EMERALD;
        if (tps >= 17) return Material.GOLD_INGOT;
        if (tps >= 15) return Material.COPPER_INGOT;
        return Material.REDSTONE;
    }

    private String getMsptColor(double mspt) {
        if (mspt <= 30) return "<green>";
        if (mspt <= 40) return "<yellow>";
        if (mspt <= 50) return "<gold>";
        return "<red>";
    }

    private String getMemoryColor(double percent) {
        if (percent <= 60) return "<green>";
        if (percent <= 75) return "<yellow>";
        if (percent <= 85) return "<gold>";
        return "<red>";
    }

    private String getCpuColor(double percent) {
        if (percent <= 50) return "<green>";
        if (percent <= 70) return "<yellow>";
        if (percent <= 85) return "<gold>";
        return "<red>";
    }

    private String getScoreColor(int score) {
        if (score >= 90) return "<green>";
        if (score >= 70) return "<yellow>";
        if (score >= 50) return "<gold>";
        return "<red>";
    }
}
