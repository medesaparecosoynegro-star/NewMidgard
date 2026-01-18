package me.ray.midgard.modules.performance.gui;

import me.ray.midgard.core.gui.BaseGui;
import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.core.utils.ItemBuilder;
import me.ray.midgard.modules.performance.PerformanceModule;
import me.ray.midgard.modules.performance.spark.SparkPerformanceManager;
import me.ray.midgard.modules.performance.spark.SparkPerformanceManager.*;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI detalhada para cada tipo de métrica.
 * Permite visualização aprofundada de TPS, MSPT, Memory, CPU, GC e Diagnóstico.
 */
public class MetricsDetailGui extends BaseGui {

    public enum MetricType {
        TPS("⏱ TPS Monitor", Material.CLOCK),
        MSPT("⚡ MSPT Monitor", Material.LIGHTNING_ROD),
        MEMORY("💾 Memory Monitor", Material.EMERALD_BLOCK),
        CPU("💻 CPU Monitor", Material.REDSTONE_TORCH),
        GC("🗑 Garbage Collector", Material.HOPPER),
        DIAGNOSE("🩺 Diagnóstico", Material.GOLDEN_APPLE);

        private final String title;
        private final Material icon;

        MetricType(String title, Material icon) {
            this.title = title;
            this.icon = icon;
        }

        public String getTitle() { return title; }
        public Material getIcon() { return icon; }
    }

    private final PerformanceModule module;
    private final MetricType type;

    public MetricsDetailGui(Player player, PerformanceModule module, MetricType type) {
        super(player, 6, "<gradient:#ff6b6b:#feca57>" + type.getTitle() + "</gradient>");
        this.module = module;
        this.type = type;
    }

    @Override
    public void initializeItems() {
        fillBackground();
        
        switch (type) {
            case TPS -> buildTPSView();
            case MSPT -> buildMSPTView();
            case MEMORY -> buildMemoryView();
            case CPU -> buildCPUView();
            case GC -> buildGCView();
            case DIAGNOSE -> buildDiagnoseView();
        }

        addNavigationItems();
    }

    private void fillBackground() {
        ItemStack darkPane = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setName(" ")
                .build();
        
        ItemStack accentPane = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setName(" ")
                .build();

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, darkPane);
        }

        // Top accent line
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, accentPane);
        }
    }

    // ===== TPS VIEW =====
    private void buildTPSView() {
        if (!SparkPerformanceManager.isAvailable()) {
            addSparkWarning();
            return;
        }

        var metrics = SparkPerformanceManager.getInstance().getMetrics();
        var tps = metrics.tps();

        if (!tps.available()) {
            addUnavailableMessage("TPS");
            return;
        }

        // Main TPS display (center)
        double currentTps = tps.last5s();
        String tpsColor = getTpsColor(currentTps);

        List<String> mainLore = new ArrayList<>();
        mainLore.add("");
        mainLore.add("<gray>Ticks Por Segundo atual");
        mainLore.add("");
        mainLore.add(tpsColor + "<bold>" + String.format("%.2f", currentTps) + " TPS</bold>");
        mainLore.add("");
        mainLore.add(createLargeProgressBar(currentTps / 20.0, tpsColor));
        mainLore.add("");
        mainLore.add("<dark_gray>Meta: <white>20.00 TPS");

        inventory.setItem(13, new ItemBuilder(getTpsMaterial(currentTps))
                .setName(tpsColor + "⏱ TPS Atual")
                .lore(parseLore(mainLore))
                .glow()
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        // Time windows
        int[] slots = {29, 30, 31, 32, 33};
        double[] values = {tps.last5s(), tps.last10s(), tps.last1m(), tps.last5m(), tps.last15m()};
        String[] labels = {"5 segundos", "10 segundos", "1 minuto", "5 minutos", "15 minutos"};
        Material[] materials = {Material.CLOCK, Material.CLOCK, Material.CLOCK, Material.CLOCK, Material.CLOCK};

        for (int i = 0; i < 5; i++) {
            String color = getTpsColor(values[i]);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("<gray>Média de TPS nos últimos");
            lore.add("<white>" + labels[i]);
            lore.add("");
            lore.add(color + String.format("%.2f", values[i]) + " TPS");
            lore.add("");
            lore.add(createProgressBar(values[i] / 20.0, color));

            inventory.setItem(slots[i], new ItemBuilder(materials[i])
                    .setName("<yellow>⏱ " + labels[i])
                    .lore(parseLore(lore))
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .build());
        }

        // Info panel
        List<String> infoLore = new ArrayList<>();
        infoLore.add("");
        infoLore.add("<gray>O TPS (Ticks Per Second) mede");
        infoLore.add("<gray>quantos ticks o servidor processa");
        infoLore.add("<gray>por segundo.");
        infoLore.add("");
        infoLore.add("<green>20 TPS <dark_gray>= Perfeito");
        infoLore.add("<yellow>17-19 TPS <dark_gray>= Bom");
        infoLore.add("<gold>15-17 TPS <dark_gray>= Lag leve");
        infoLore.add("<red><15 TPS <dark_gray>= Lag severo");

        inventory.setItem(22, new ItemBuilder(Material.BOOK)
                .setName("<aqua>ℹ O que é TPS?")
                .lore(parseLore(infoLore))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());
    }

    // ===== MSPT VIEW =====
    private void buildMSPTView() {
        if (!SparkPerformanceManager.isAvailable()) {
            addSparkWarning();
            return;
        }

        var metrics = SparkPerformanceManager.getInstance().getMetrics();
        var mspt = metrics.mspt();

        if (!mspt.available()) {
            addUnavailableMessage("MSPT");
            return;
        }

        var last10s = mspt.last10s();
        var last1m = mspt.last1m();

        // Main MSPT (median)
        double medianMspt = last10s.median();
        String msptColor = getMsptColor(medianMspt);

        List<String> mainLore = new ArrayList<>();
        mainLore.add("");
        mainLore.add("<gray>Tempo por tick (mediana)");
        mainLore.add("");
        mainLore.add(msptColor + "<bold>" + String.format("%.2f", medianMspt) + "ms</bold>");
        mainLore.add("");
        mainLore.add(createLargeProgressBar(1 - (medianMspt / 50.0), msptColor));
        mainLore.add("");
        mainLore.add("<dark_gray>Limite: <white>50ms / tick");

        inventory.setItem(13, new ItemBuilder(Material.LIGHTNING_ROD)
                .setName(msptColor + "⚡ MSPT Atual")
                .lore(parseLore(mainLore))
                .glow()
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        // 10 seconds window details
        String[] metricNames = {"Mínimo", "Mediana", "P95", "Máximo"};
        double[] values10s = {last10s.min(), last10s.median(), last10s.p95(), last10s.max()};
        Material[] mats = {Material.LIME_CONCRETE, Material.YELLOW_CONCRETE, Material.ORANGE_CONCRETE, Material.RED_CONCRETE};
        int[] slots10s = {28, 29, 30, 31};

        for (int i = 0; i < 4; i++) {
            String color = getMsptColor(values10s[i]);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("<gray>Últimos 10 segundos");
            lore.add("");
            lore.add(color + String.format("%.2f", values10s[i]) + "ms");

            inventory.setItem(slots10s[i], new ItemBuilder(mats[i])
                    .setName("<yellow>" + metricNames[i] + " (10s)")
                    .lore(parseLore(lore))
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .build());
        }

        // 1 minute window details
        double[] values1m = {last1m.min(), last1m.median(), last1m.p95(), last1m.max()};
        int[] slots1m = {33, 34, 35, 36};

        for (int i = 0; i < 4; i++) {
            String color = getMsptColor(values1m[i]);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("<gray>Último minuto");
            lore.add("");
            lore.add(color + String.format("%.2f", values1m[i]) + "ms");

            inventory.setItem(slots1m[i], new ItemBuilder(mats[i])
                    .setName("<gold>" + metricNames[i] + " (1m)")
                    .lore(parseLore(lore))
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .build());
        }

        // Info panel
        List<String> infoLore = new ArrayList<>();
        infoLore.add("");
        infoLore.add("<gray>MSPT (Milliseconds Per Tick)");
        infoLore.add("<gray>mede quanto tempo o servidor");
        infoLore.add("<gray>leva para processar cada tick.");
        infoLore.add("");
        infoLore.add("<green><30ms <dark_gray>= Excelente");
        infoLore.add("<yellow>30-40ms <dark_gray>= Bom");
        infoLore.add("<gold>40-50ms <dark_gray>= Limite");
        infoLore.add("<red>>50ms <dark_gray>= Lag!");

        inventory.setItem(22, new ItemBuilder(Material.BOOK)
                .setName("<aqua>ℹ O que é MSPT?")
                .lore(parseLore(infoLore))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());
    }

    // ===== MEMORY VIEW =====
    private void buildMemoryView() {
        if (!SparkPerformanceManager.isAvailable()) {
            addSparkWarning();
            return;
        }

        var metrics = SparkPerformanceManager.getInstance().getMetrics();
        var mem = metrics.memory();

        if (!mem.available()) {
            addUnavailableMessage("Memory");
            return;
        }

        double percent = mem.usedPercent();
        String memColor = getMemoryColor(percent);

        // Main memory display
        List<String> mainLore = new ArrayList<>();
        mainLore.add("");
        mainLore.add("<gray>Uso atual de memória Heap");
        mainLore.add("");
        mainLore.add(memColor + "<bold>" + String.format("%.1f%%", percent) + "</bold>");
        mainLore.add("");
        mainLore.add(createLargeProgressBar(percent / 100.0, memColor));
        mainLore.add("");
        mainLore.add(memColor + mem.usedMB() + "MB <gray>/ <white>" + mem.maxMB() + "MB");

        Material memMat = percent > 85 ? Material.REDSTONE_BLOCK : 
                          percent > 70 ? Material.GOLD_BLOCK : Material.EMERALD_BLOCK;

        inventory.setItem(13, new ItemBuilder(memMat)
                .setName(memColor + "💾 Memória Heap")
                .lore(parseLore(mainLore))
                .glow()
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        // Memory breakdown
        List<String> usedLore = new ArrayList<>();
        usedLore.add("");
        usedLore.add("<gray>Memória em uso pelo JVM");
        usedLore.add("");
        usedLore.add("<red>" + mem.usedMB() + " MB");

        inventory.setItem(29, new ItemBuilder(Material.RED_CONCRETE)
                .setName("<red>Usado")
                .lore(parseLore(usedLore))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        List<String> freeLore = new ArrayList<>();
        freeLore.add("");
        freeLore.add("<gray>Memória disponível");
        freeLore.add("");
        freeLore.add("<green>" + mem.freeMB() + " MB");

        inventory.setItem(30, new ItemBuilder(Material.LIME_CONCRETE)
                .setName("<green>Livre")
                .lore(parseLore(freeLore))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        List<String> allocLore = new ArrayList<>();
        allocLore.add("");
        allocLore.add("<gray>Memória alocada pelo SO");
        allocLore.add("");
        allocLore.add("<aqua>" + mem.totalMB() + " MB");

        inventory.setItem(32, new ItemBuilder(Material.LIGHT_BLUE_CONCRETE)
                .setName("<aqua>Alocado")
                .lore(parseLore(allocLore))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        List<String> maxLore = new ArrayList<>();
        maxLore.add("");
        maxLore.add("<gray>Máximo definido (-Xmx)");
        maxLore.add("");
        maxLore.add("<white>" + mem.maxMB() + " MB");

        inventory.setItem(33, new ItemBuilder(Material.WHITE_CONCRETE)
                .setName("<white>Máximo (Xmx)")
                .lore(parseLore(maxLore))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        // GC shortcut
        var gc = metrics.gc();
        if (gc.available()) {
            List<String> gcLore = new ArrayList<>();
            gcLore.add("");
            gcLore.add("<gray>Coletas: <white>" + gc.totalCollections());
            gcLore.add("<gray>Tempo: <white>" + gc.formatTime());
            gcLore.add("");
            gcLore.add("<yellow>Clique para detalhes de GC");

            inventory.setItem(40, new ItemBuilder(Material.HOPPER)
                    .setName("<gradient:#94a3b8:#64748b>🗑 Garbage Collector</gradient>")
                    .lore(parseLore(gcLore))
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .build());
        }
    }

    // ===== CPU VIEW =====
    private void buildCPUView() {
        if (!SparkPerformanceManager.isAvailable()) {
            addSparkWarning();
            return;
        }

        var metrics = SparkPerformanceManager.getInstance().getMetrics();
        var cpu = metrics.cpu();

        if (!cpu.available()) {
            addUnavailableMessage("CPU");
            return;
        }

        var process = cpu.process();
        var system = cpu.system();

        double processPercent = process.seconds10() * 100;
        String cpuColor = getCpuColor(processPercent);

        // Main CPU display
        List<String> mainLore = new ArrayList<>();
        mainLore.add("");
        mainLore.add("<gray>Uso de CPU pelo processo Java");
        mainLore.add("");
        mainLore.add(cpuColor + "<bold>" + String.format("%.1f%%", processPercent) + "</bold>");
        mainLore.add("");
        mainLore.add(createLargeProgressBar(processPercent / 100.0, cpuColor));

        inventory.setItem(13, new ItemBuilder(Material.REDSTONE_TORCH)
                .setName(cpuColor + "💻 CPU Processo")
                .lore(parseLore(mainLore))
                .glow()
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        // Process CPU windows
        String[] labels = {"10 segundos", "1 minuto", "15 minutos"};
        double[] processValues = {process.seconds10() * 100, process.minutes1() * 100, process.minutes15() * 100};
        int[] processSlots = {28, 29, 30};

        for (int i = 0; i < 3; i++) {
            String color = getCpuColor(processValues[i]);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("<gray>Média do processo nos últimos");
            lore.add("<white>" + labels[i]);
            lore.add("");
            lore.add(color + String.format("%.1f%%", processValues[i]));
            lore.add("");
            lore.add(createProgressBar(processValues[i] / 100.0, color));

            inventory.setItem(processSlots[i], new ItemBuilder(Material.COMPARATOR)
                    .setName("<gradient:#f472b6:#a78bfa>Processo - " + labels[i] + "</gradient>")
                    .lore(parseLore(lore))
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .build());
        }

        // System CPU windows
        double[] systemValues = {system.seconds10() * 100, system.minutes1() * 100, system.minutes15() * 100};
        int[] systemSlots = {32, 33, 34};

        for (int i = 0; i < 3; i++) {
            String color = getCpuColor(systemValues[i]);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add("<gray>Média do sistema nos últimos");
            lore.add("<white>" + labels[i]);
            lore.add("");
            lore.add(color + String.format("%.1f%%", systemValues[i]));
            lore.add("");
            lore.add(createProgressBar(systemValues[i] / 100.0, color));

            inventory.setItem(systemSlots[i], new ItemBuilder(Material.REPEATER)
                    .setName("<gradient:#22d3ee:#3b82f6>Sistema - " + labels[i] + "</gradient>")
                    .lore(parseLore(lore))
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .build());
        }
    }

    // ===== GC VIEW =====
    private void buildGCView() {
        if (!SparkPerformanceManager.isAvailable()) {
            addSparkWarning();
            return;
        }

        var metrics = SparkPerformanceManager.getInstance().getMetrics();
        var gc = metrics.gc();

        if (!gc.available()) {
            addUnavailableMessage("Garbage Collector");
            return;
        }

        // Main GC display
        List<String> mainLore = new ArrayList<>();
        mainLore.add("");
        mainLore.add("<gray>Estatísticas de Garbage Collection");
        mainLore.add("");
        mainLore.add("<white>Total de coletas: <green>" + gc.totalCollections());
        mainLore.add("<white>Tempo total: <yellow>" + gc.formatTime());
        mainLore.add("<white>Tempo médio: <aqua>" + String.format("%.2f", gc.avgTime()) + "ms");
        mainLore.add("");
        
        double freq = gc.avgFrequency() / 1000.0;
        String freqColor = freq < 10 ? "<red>" : freq < 30 ? "<yellow>" : "<green>";
        mainLore.add("<white>Frequência: " + freqColor + String.format("%.1f", freq) + "s entre coletas");

        inventory.setItem(13, new ItemBuilder(Material.HOPPER)
                .setName("<gradient:#94a3b8:#64748b>🗑 Garbage Collector</gradient>")
                .lore(parseLore(mainLore))
                .glow()
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        // Individual collectors
        var collectors = gc.collectors();
        if (!collectors.isEmpty()) {
            int slot = 29;
            for (var entry : collectors.entrySet()) {
                if (slot > 35) break;
                
                var collector = entry.getValue();
                List<String> collectorLore = new ArrayList<>();
                collectorLore.add("");
                collectorLore.add("<gray>Coletor de garbage individual");
                collectorLore.add("");
                collectorLore.add("<white>Coletas: <green>" + collector.totalCollections());
                collectorLore.add("<white>Tempo total: <yellow>" + collector.totalTime() + "ms");
                collectorLore.add("<white>Tempo médio: <aqua>" + String.format("%.2f", collector.avgTime()) + "ms");

                inventory.setItem(slot++, new ItemBuilder(Material.MINECART)
                        .setName("<yellow>" + entry.getKey())
                        .lore(parseLore(collectorLore))
                        .flags(ItemFlag.HIDE_ATTRIBUTES)
                        .build());
            }
        }

        // Info panel
        List<String> infoLore = new ArrayList<>();
        infoLore.add("");
        infoLore.add("<gray>O Garbage Collector libera");
        infoLore.add("<gray>memória de objetos não usados.");
        infoLore.add("");
        infoLore.add("<green>Frequência > 60s <dark_gray>= Normal");
        infoLore.add("<yellow>30-60s <dark_gray>= Moderado");
        infoLore.add("<red><30s <dark_gray>= Memory pressure");

        inventory.setItem(22, new ItemBuilder(Material.BOOK)
                .setName("<aqua>ℹ O que é GC?")
                .lore(parseLore(infoLore))
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());
    }

    // ===== DIAGNOSE VIEW =====
    private void buildDiagnoseView() {
        if (!SparkPerformanceManager.isAvailable()) {
            addSparkWarning();
            return;
        }

        var diagnosis = SparkPerformanceManager.getInstance().diagnose();

        // Overall health
        HealthLevel overall = diagnosis.overallHealth();
        List<String> overallLore = new ArrayList<>();
        overallLore.add("");
        overallLore.add("<gray>Status geral do servidor");
        overallLore.add("");
        overallLore.add(overall.getColor() + overall.getIcon() + " " + overall.getLabel());

        inventory.setItem(13, new ItemBuilder(getHealthMaterial(overall))
                .setName(overall.getColor() + "✦ Saúde Geral")
                .lore(parseLore(overallLore))
                .glow()
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        // Individual diagnostics
        HealthIssue[] issues = {diagnosis.tps(), diagnosis.mspt(), diagnosis.cpu(), diagnosis.memory(), diagnosis.gc()};
        String[] names = {"TPS", "MSPT", "CPU", "Memória", "GC"};
        Material[] mats = {Material.CLOCK, Material.LIGHTNING_ROD, Material.REDSTONE_TORCH, Material.EMERALD_BLOCK, Material.HOPPER};
        int[] slots = {29, 30, 31, 32, 33};

        for (int i = 0; i < 5; i++) {
            HealthIssue issue = issues[i];
            List<String> issueLore = new ArrayList<>();
            issueLore.add("");
            issueLore.add(issue.getColor() + issue.getIcon() + " " + issue.message());
            issueLore.add("");
            issueLore.add("<dark_gray>Componente: <white>" + issue.category());
            issueLore.add("<dark_gray>Nível: " + issue.level().getColor() + issue.level().getLabel());

            inventory.setItem(slots[i], new ItemBuilder(mats[i])
                    .setName(issue.getColor() + names[i])
                    .lore(parseLore(issueLore))
                    .glowIf(issue.level().ordinal() >= HealthLevel.WARNING.ordinal())
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .build());
        }
    }

    private void addNavigationItems() {
        // Back button
        inventory.setItem(45, new ItemBuilder(Material.ARROW)
                .setName("<yellow>← Voltar")
                .addLore("")
                .addLore("<gray>Voltar ao Dashboard")
                .build());

        // Refresh
        inventory.setItem(53, new ItemBuilder(Material.SUNFLOWER)
                .setName("<gradient:#fbbf24:#f59e0b>🔄 Atualizar</gradient>")
                .addLore("")
                .addLore("<gray>Atualizar dados")
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

        inventory.setItem(22, new ItemBuilder(Material.BARRIER)
                .setName("<red>⚠ Spark Necessário</red>")
                .lore(parseLore(lore))
                .glow()
                .build());
    }

    private void addUnavailableMessage(String metric) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("<red>" + metric + " não disponível");
        lore.add("");
        lore.add("<gray>Esta métrica não está sendo");
        lore.add("<gray>coletada pelo Spark no momento.");

        inventory.setItem(22, new ItemBuilder(Material.GRAY_DYE)
                .setName("<red>⚠ Indisponível</red>")
                .lore(parseLore(lore))
                .build());
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player clicker)) return;
        if (!clicker.equals(this.player)) return;

        int slot = event.getRawSlot();

        switch (slot) {
            case 40 -> { // GC shortcut from memory view
                if (type == MetricType.MEMORY) {
                    clicker.playSound(clicker.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
                    new MetricsDetailGui(clicker, module, MetricType.GC).open();
                }
            }
            case 45 -> { // Back
                clicker.playSound(clicker.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                new PerformanceMainGui(clicker, module).open();
            }
            case 53 -> { // Refresh
                clicker.playSound(clicker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
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

    private String createLargeProgressBar(double percent, String color) {
        percent = Math.max(0, Math.min(1, percent));
        int filled = (int) (percent * 30);
        StringBuilder bar = new StringBuilder("<dark_gray>▐");
        for (int i = 0; i < 30; i++) {
            if (i < filled) {
                bar.append(color).append("█");
            } else {
                bar.append("<dark_gray>░");
            }
        }
        bar.append("<dark_gray>▌");
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
}
