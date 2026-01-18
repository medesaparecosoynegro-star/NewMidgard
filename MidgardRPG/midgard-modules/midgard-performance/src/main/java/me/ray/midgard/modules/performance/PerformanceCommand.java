package me.ray.midgard.modules.performance;

import me.ray.midgard.core.command.MidgardCommand;
import me.ray.midgard.core.debug.MidgardProfiler;
import me.ray.midgard.core.text.MessageUtils;
import me.ray.midgard.modules.performance.gui.PerformanceMainGui;
import me.ray.midgard.modules.performance.spark.*;
import me.ray.midgard.modules.performance.spark.MidgardAnalyzer.*;
import me.ray.midgard.modules.performance.spark.PerformanceReport.*;
import me.ray.midgard.modules.performance.spark.SparkPerformanceManager.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Comando principal do módulo de Performance.
 * Baseado 100% no Spark para métricas precisas.
 * 
 * Subcomandos:
 * - /perf - Dashboard completo
 * - /perf tps - TPS detalhado
 * - /perf mspt - MSPT detalhado
 * - /perf memory - Uso de memória
 * - /perf cpu - Uso de CPU
 * - /perf gc - Garbage Collection
 * - /perf modules - Análise de módulos
 * - /perf events - Análise de eventos
 * - /perf commands - Análise de comandos
 * - /perf profiler - Top operações
 * - /perf report - Relatório completo
 * - /perf diagnose - Diagnóstico de saúde
 * - /perf issues - Lista problemas detectados
 * - /perf clear - Limpa estatísticas
 */
public class PerformanceCommand extends MidgardCommand {

    private static final String PREFIX = "<dark_gray>[<gradient:#ff6b6b:#feca57>⚡Perf</gradient><dark_gray>] ";
    private static final String HEADER = "<gradient:#ff6b6b:#feca57><bold>═══════════════════════════════════════</bold></gradient>";
    
    private final PerformanceModule module;

    public PerformanceCommand(PerformanceModule module) {
        super("performance", "midgard.admin", false);
        this.module = module;
    }

    @Override
    public List<String> getAliases() {
        return java.util.Collections.singletonList("perf");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendDashboard(sender);
            return;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "gui", "menu" -> openGui(sender);
            case "tps" -> sendTPS(sender);
            case "mspt" -> sendMSPT(sender);
            case "memory", "mem" -> sendMemory(sender);
            case "cpu" -> sendCPU(sender);
            case "gc" -> sendGC(sender);
            case "modules", "mods" -> sendModules(sender);
            case "events" -> sendEvents(sender);
            case "commands", "cmds" -> sendCommands(sender);
            case "profiler", "profile" -> sendProfiler(sender);
            case "report" -> sendFullReport(sender);
            case "diagnose", "diag" -> sendDiagnose(sender);
            case "issues" -> sendIssues(sender);
            case "watcher" -> sendWatcher(sender);
            case "clear" -> clearStats(sender);
            case "help" -> sendHelp(sender);
            default -> sendHelp(sender);
        }
    }

    // ========== GUI ==========
    
    private void openGui(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            MessageUtils.send(sender, PREFIX + "<red>Este comando só pode ser usado por jogadores.");
            return;
        }
        
        new PerformanceMainGui(player, module).open();
    }

    // ========== DASHBOARD ==========
    
    private void sendDashboard(CommandSender sender) {
        if (!checkSpark(sender)) return;
        
        var manager = SparkPerformanceManager.getInstance();
        var metrics = manager.getMetrics();
        var diagnosis = manager.diagnose();
        var report = PerformanceReport.generateQuickReport();
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, HEADER);
        MessageUtils.send(sender, "<gradient:#ff6b6b:#feca57><bold>     ⚡ MIDGARD PERFORMANCE DASHBOARD ⚡</bold></gradient>");
        MessageUtils.send(sender, HEADER);
        MessageUtils.send(sender, "");
        
        // Status geral
        String healthIcon = diagnosis.overallHealth().getIcon();
        String healthColor = diagnosis.overallHealth().getColor();
        MessageUtils.send(sender, "  " + healthColor + healthIcon + " <white>Saúde: " + 
            healthColor + diagnosis.overallHealth().getLabel());
        MessageUtils.send(sender, "");
        
        // TPS
        var tps = metrics.tps();
        if (tps.available()) {
            MessageUtils.send(sender, "  <gray>⏱ TPS:");
            MessageUtils.send(sender, "    " + tps.getColor(tps.last5s()) + String.format("%.1f", tps.last5s()) + 
                " <dark_gray>(5s) │ " + tps.getColor(tps.last1m()) + String.format("%.1f", tps.last1m()) + 
                " <dark_gray>(1m) │ " + tps.getColor(tps.last5m()) + String.format("%.1f", tps.last5m()) + " <dark_gray>(5m)");
            sendTPSBar(sender, tps.last5s());
        }
        
        MessageUtils.send(sender, "");
        
        // MSPT
        var mspt = metrics.mspt();
        if (mspt.available()) {
            var w = mspt.last10s();
            MessageUtils.send(sender, "  <gray>⚡ MSPT: " + 
                w.getColor(w.median()) + String.format("%.1f", w.median()) + "ms <gray>mediana │ " +
                w.getColor(w.p95()) + String.format("%.1f", w.p95()) + "ms <gray>p95 │ " +
                w.getColor(w.max()) + String.format("%.1f", w.max()) + "ms <gray>max");
        }
        
        // Memory
        var mem = metrics.memory();
        if (mem.available()) {
            MessageUtils.send(sender, "  <gray>💾 RAM: " + 
                mem.getColor() + mem.usedMB() + "MB" + 
                "<gray>/" + mem.maxMB() + "MB <dark_gray>(" + 
                mem.getColor() + String.format("%.1f%%", mem.usedPercent()) + "<dark_gray>)");
        }
        
        // CPU
        var cpu = metrics.cpu();
        if (cpu.available()) {
            var p = cpu.process();
            MessageUtils.send(sender, "  <gray>💻 CPU: " + 
                p.getColor(p.seconds10()) + p.formatPercent(p.seconds10()) + 
                " <dark_gray>(processo) │ " +
                cpu.system().getColor(cpu.system().seconds10()) + cpu.system().formatPercent(cpu.system().seconds10()) + 
                " <dark_gray>(sistema)");
        }
        
        // GC
        var gc = metrics.gc();
        if (gc.available()) {
            MessageUtils.send(sender, "  <gray>🗑 GC: <white>" + gc.totalCollections() + 
                " <gray>coletas │ <white>" + gc.formatTime() + " <gray>total");
        }
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, "  <dark_gray>Use <yellow>/perf help <dark_gray>para ver todos os comandos");
        MessageUtils.send(sender, "");
    }

    // ========== TPS DETALHADO ==========
    
    private void sendTPS(CommandSender sender) {
        if (!checkSpark(sender)) return;
        
        var metrics = SparkPerformanceManager.getInstance().getMetrics();
        var tps = metrics.tps();
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, PREFIX + "<white>⏱ TPS Monitor <dark_gray>(Spark)");
        MessageUtils.send(sender, "");
        
        if (!tps.available()) {
            MessageUtils.send(sender, "  <red>TPS não disponível");
            return;
        }
        
        MessageUtils.send(sender, "  <gray>Janelas de tempo:");
        MessageUtils.send(sender, "    <dark_gray>5s:  " + tps.getColor(tps.last5s()) + String.format("%.2f", tps.last5s()));
        MessageUtils.send(sender, "    <dark_gray>10s: " + tps.getColor(tps.last10s()) + String.format("%.2f", tps.last10s()));
        MessageUtils.send(sender, "    <dark_gray>1m:  " + tps.getColor(tps.last1m()) + String.format("%.2f", tps.last1m()));
        MessageUtils.send(sender, "    <dark_gray>5m:  " + tps.getColor(tps.last5m()) + String.format("%.2f", tps.last5m()));
        MessageUtils.send(sender, "    <dark_gray>15m: " + tps.getColor(tps.last15m()) + String.format("%.2f", tps.last15m()));
        MessageUtils.send(sender, "");
        
        sendTPSBar(sender, tps.last5s());
        
        // Análise
        MessageUtils.send(sender, "");
        var diagnosis = SparkPerformanceManager.getInstance().diagnose().tps();
        MessageUtils.send(sender, "  <gray>Diagnóstico: " + diagnosis.getColor() + diagnosis.message());
        MessageUtils.send(sender, "");
    }

    // ========== MSPT DETALHADO ==========
    
    private void sendMSPT(CommandSender sender) {
        if (!checkSpark(sender)) return;
        
        var metrics = SparkPerformanceManager.getInstance().getMetrics();
        var mspt = metrics.mspt();
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, PREFIX + "<white>⚡ MSPT Monitor <dark_gray>(Milliseconds Per Tick)");
        MessageUtils.send(sender, "");
        
        if (!mspt.available()) {
            MessageUtils.send(sender, "  <red>MSPT não disponível");
            return;
        }
        
        // Últimos 10 segundos
        var w10s = mspt.last10s();
        MessageUtils.send(sender, "  <yellow>Últimos 10 segundos:");
        MessageUtils.send(sender, "    <gray>Min: " + w10s.getColor(w10s.min()) + String.format("%.2f", w10s.min()) + "ms");
        MessageUtils.send(sender, "    <gray>Mediana: " + w10s.getColor(w10s.median()) + String.format("%.2f", w10s.median()) + "ms");
        MessageUtils.send(sender, "    <gray>P95: " + w10s.getColor(w10s.p95()) + String.format("%.2f", w10s.p95()) + "ms");
        MessageUtils.send(sender, "    <gray>Max: " + w10s.getColor(w10s.max()) + String.format("%.2f", w10s.max()) + "ms");
        MessageUtils.send(sender, "");
        
        // Último minuto
        var w1m = mspt.last1m();
        MessageUtils.send(sender, "  <yellow>Último minuto:");
        MessageUtils.send(sender, "    <gray>Min: " + w1m.getColor(w1m.min()) + String.format("%.2f", w1m.min()) + "ms");
        MessageUtils.send(sender, "    <gray>Mediana: " + w1m.getColor(w1m.median()) + String.format("%.2f", w1m.median()) + "ms");
        MessageUtils.send(sender, "    <gray>P95: " + w1m.getColor(w1m.p95()) + String.format("%.2f", w1m.p95()) + "ms");
        MessageUtils.send(sender, "    <gray>Max: " + w1m.getColor(w1m.max()) + String.format("%.2f", w1m.max()) + "ms");
        MessageUtils.send(sender, "");
        
        // Nota explicativa
        MessageUtils.send(sender, "  <dark_gray>⚠ 50ms = 1 tick. Valores > 50ms causam lag.");
        MessageUtils.send(sender, "");
    }

    // ========== MEMORY ==========
    
    private void sendMemory(CommandSender sender) {
        if (!checkSpark(sender)) return;
        
        var metrics = SparkPerformanceManager.getInstance().getMetrics();
        var mem = metrics.memory();
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, PREFIX + "<white>💾 Memory Monitor");
        MessageUtils.send(sender, "");
        
        MessageUtils.send(sender, "  <gray>Usado: " + mem.getColor() + mem.usedMB() + " MB");
        MessageUtils.send(sender, "  <gray>Livre: <green>" + mem.freeMB() + " MB");
        MessageUtils.send(sender, "  <gray>Total Alocado: <aqua>" + mem.totalMB() + " MB");
        MessageUtils.send(sender, "  <gray>Máximo (Xmx): <white>" + mem.maxMB() + " MB");
        MessageUtils.send(sender, "");
        
        // Barra visual
        sendMemoryBar(sender, mem.usedPercent());
        
        // GC Info
        var gc = metrics.gc();
        if (gc.available()) {
            MessageUtils.send(sender, "");
            MessageUtils.send(sender, "  <gray>Garbage Collection:");
            MessageUtils.send(sender, "    <dark_gray>Coletas: <white>" + gc.totalCollections());
            MessageUtils.send(sender, "    <dark_gray>Tempo total: <white>" + gc.formatTime());
            MessageUtils.send(sender, "    <dark_gray>Tempo médio: <white>" + String.format("%.2f", gc.avgTime()) + "ms");
        }
        MessageUtils.send(sender, "");
    }

    // ========== CPU ==========
    
    private void sendCPU(CommandSender sender) {
        if (!checkSpark(sender)) return;
        
        var metrics = SparkPerformanceManager.getInstance().getMetrics();
        var cpu = metrics.cpu();
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, PREFIX + "<white>💻 CPU Monitor");
        MessageUtils.send(sender, "");
        
        if (!cpu.available()) {
            MessageUtils.send(sender, "  <red>CPU não disponível");
            return;
        }
        
        var p = cpu.process();
        var s = cpu.system();
        
        MessageUtils.send(sender, "  <yellow>Processo Java:");
        MessageUtils.send(sender, "    <dark_gray>10s: " + p.getColor(p.seconds10()) + p.formatPercent(p.seconds10()));
        MessageUtils.send(sender, "    <dark_gray>1m:  " + p.getColor(p.minutes1()) + p.formatPercent(p.minutes1()));
        MessageUtils.send(sender, "    <dark_gray>15m: " + p.getColor(p.minutes15()) + p.formatPercent(p.minutes15()));
        MessageUtils.send(sender, "");
        
        MessageUtils.send(sender, "  <yellow>Sistema:");
        MessageUtils.send(sender, "    <dark_gray>10s: " + s.getColor(s.seconds10()) + s.formatPercent(s.seconds10()));
        MessageUtils.send(sender, "    <dark_gray>1m:  " + s.getColor(s.minutes1()) + s.formatPercent(s.minutes1()));
        MessageUtils.send(sender, "    <dark_gray>15m: " + s.getColor(s.minutes15()) + s.formatPercent(s.minutes15()));
        MessageUtils.send(sender, "");
    }

    // ========== GC ==========
    
    private void sendGC(CommandSender sender) {
        if (!checkSpark(sender)) return;
        
        var metrics = SparkPerformanceManager.getInstance().getMetrics();
        var gc = metrics.gc();
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, PREFIX + "<white>🗑 Garbage Collection");
        MessageUtils.send(sender, "");
        
        if (!gc.available()) {
            MessageUtils.send(sender, "  <red>GC não disponível");
            return;
        }
        
        MessageUtils.send(sender, "  <gray>Total de coletas: <white>" + gc.totalCollections());
        MessageUtils.send(sender, "  <gray>Tempo total em GC: <white>" + gc.formatTime());
        MessageUtils.send(sender, "  <gray>Tempo médio por coleta: <white>" + String.format("%.2f", gc.avgTime()) + "ms");
        MessageUtils.send(sender, "  <gray>Frequência média: <white>" + String.format("%.1f", gc.avgFrequency() / 1000.0) + "s");
        MessageUtils.send(sender, "");
        
        // Coletores individuais
        if (!gc.collectors().isEmpty()) {
            MessageUtils.send(sender, "  <yellow>Coletores:");
            for (var entry : gc.collectors().entrySet()) {
                var collector = entry.getValue();
                MessageUtils.send(sender, "    <gray>" + entry.getKey() + ": <white>" + 
                    collector.totalCollections() + " <gray>coletas, " +
                    "<white>" + String.format("%.2f", collector.avgTime()) + "ms <gray>média");
            }
        }
        MessageUtils.send(sender, "");
    }

    // ========== MODULES ==========
    
    private void sendModules(CommandSender sender) {
        var analyzer = MidgardAnalyzer.getInstance();
        if (analyzer == null) {
            MessageUtils.send(sender, PREFIX + "<red>Analyzer não inicializado");
            return;
        }
        
        var analysis = analyzer.analyze();
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, PREFIX + "<white>📦 Análise de Módulos");
        MessageUtils.send(sender, "");
        
        if (analysis.modules().isEmpty()) {
            MessageUtils.send(sender, "  <gray>Nenhum módulo encontrado.");
            return;
        }
        
        for (var mod : analysis.modules()) {
            String status = mod.enabled() ? "<green>✔" : "<red>✘";
            String healthIcon = mod.health().getIcon();
            String healthColor = mod.health().getColor();
            
            MessageUtils.send(sender, "  " + status + " " + healthColor + healthIcon + " <white>" + mod.name());
            MessageUtils.send(sender, "      <dark_gray>Init: " + getTimeColor(mod.enableTime()) + mod.enableTime() + "ms" +
                " <dark_gray>│ Ops: <gray>" + mod.totalOperations() +
                " <dark_gray>│ Listeners: <gray>" + mod.listenerCount());
            
            // Top 3 operações lentas do módulo
            if (!mod.operations().isEmpty()) {
                var slowOps = mod.operations().stream().limit(3).toList();
                for (var op : slowOps) {
                    if (op.maxTime() > 10) {
                        MessageUtils.send(sender, "        " + op.severity().getColor() + op.severity().getIcon() + 
                            " <gray>" + op.name() + ": " + op.maxTime() + "ms");
                    }
                }
            }
        }
        MessageUtils.send(sender, "");
    }

    // ========== EVENTS ==========
    
    private void sendEvents(CommandSender sender) {
        var analyzer = MidgardAnalyzer.getInstance();
        if (analyzer == null) return;
        
        var analysis = analyzer.analyze();
        var events = analysis.events();
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, PREFIX + "<white>📡 Análise de Eventos");
        MessageUtils.send(sender, "");
        
        MessageUtils.send(sender, "  <gray>Total de listeners: <white>" + events.totalListeners());
        MessageUtils.send(sender, "  <gray>Eventos únicos: <white>" + events.uniqueEvents());
        MessageUtils.send(sender, "");
        
        if (!events.slowest().isEmpty()) {
            MessageUtils.send(sender, "  <yellow>Top eventos mais lentos:");
            for (var listener : events.slowest()) {
                MessageUtils.send(sender, "    " + listener.severity().getColor() + listener.severity().getIcon() + 
                    " <white>" + listener.eventName() + " <dark_gray>(" + listener.listenerClass() + ")");
                MessageUtils.send(sender, "      <gray>Max: " + listener.maxTime() + "ms │ Execuções: " + listener.executions());
            }
        } else {
            MessageUtils.send(sender, "  <gray>Nenhum evento lento detectado.");
        }
        MessageUtils.send(sender, "");
    }

    // ========== COMMANDS ==========
    
    private void sendCommands(CommandSender sender) {
        var analyzer = MidgardAnalyzer.getInstance();
        if (analyzer == null) return;
        
        var analysis = analyzer.analyze();
        var commands = analysis.commands();
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, PREFIX + "<white>⌨ Análise de Comandos");
        MessageUtils.send(sender, "");
        
        MessageUtils.send(sender, "  <gray>Total de comandos: <white>" + commands.totalCommands());
        MessageUtils.send(sender, "  <gray>Total de execuções: <white>" + commands.totalExecutions());
        MessageUtils.send(sender, "");
        
        if (!commands.slowest().isEmpty()) {
            MessageUtils.send(sender, "  <yellow>Comandos mais lentos:");
            for (var cmd : commands.slowest()) {
                MessageUtils.send(sender, "    " + cmd.severity().getColor() + cmd.severity().getIcon() + 
                    " <white>/" + cmd.name());
                MessageUtils.send(sender, "      <gray>Max: " + cmd.maxTime() + "ms │ Execuções: " + cmd.executions());
            }
        } else {
            MessageUtils.send(sender, "  <gray>Nenhum comando lento detectado.");
        }
        MessageUtils.send(sender, "");
    }

    // ========== PROFILER ==========
    
    private void sendProfiler(CommandSender sender) {
        var analyzer = MidgardAnalyzer.getInstance();
        if (analyzer == null) return;
        
        var analysis = analyzer.analyze();
        var profiler = analysis.profiler();
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, PREFIX + "<white>⚙ Profiler Stats");
        MessageUtils.send(sender, "");
        
        MessageUtils.send(sender, "  <gray>Operações rastreadas: <white>" + profiler.trackedOperations());
        MessageUtils.send(sender, "  <gray>Total de execuções: <white>" + profiler.totalExecutions());
        MessageUtils.send(sender, "  <gray>Tempo total: <white>" + profiler.totalTime() + "ms");
        MessageUtils.send(sender, "");
        
        // Top 10 mais lentas
        MessageUtils.send(sender, "  <yellow>Top 10 operações mais lentas:");
        int i = 1;
        for (var op : profiler.slowest().stream().limit(10).toList()) {
            MessageUtils.send(sender, "    <gray>" + i++ + ". " + op.severity().getColor() + op.name());
            MessageUtils.send(sender, "       <dark_gray>Max: " + op.maxTime() + "ms │ Última: " + op.lastTime() + "ms │ Count: " + op.count());
        }
        MessageUtils.send(sender, "");
    }

    // ========== FULL REPORT ==========
    
    private void sendFullReport(CommandSender sender) {
        var report = PerformanceReport.generateFullReport();
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, HEADER);
        MessageUtils.send(sender, "<gradient:#ff6b6b:#feca57><bold>       ⚡ RELATÓRIO DE PERFORMANCE ⚡</bold></gradient>");
        MessageUtils.send(sender, HEADER);
        MessageUtils.send(sender, "");
        
        // Score geral
        MessageUtils.send(sender, "  <white>Score: " + report.getScoreColor() + report.overallScore() + 
            "/100 <dark_gray>[" + report.getScoreColor() + report.getScoreGrade() + "<dark_gray>]");
        MessageUtils.send(sender, "  <dark_gray>Gerado em: " + report.timestamp());
        MessageUtils.send(sender, "  <dark_gray>Spark: " + (report.sparkAvailable() ? "<green>Ativo" : "<red>Inativo"));
        MessageUtils.send(sender, "");
        
        // Issues
        if (!report.issues().isEmpty()) {
            MessageUtils.send(sender, "  <red>⚠ Problemas detectados: " + report.issues().size());
            for (var issue : report.issues().stream().limit(5).toList()) {
                MessageUtils.send(sender, "    " + issue.level().getColor() + issue.category().getIcon() + 
                    " " + issue.title());
            }
            if (report.issues().size() > 5) {
                MessageUtils.send(sender, "    <dark_gray>... e mais " + (report.issues().size() - 5) + " problemas");
            }
        } else {
            MessageUtils.send(sender, "  <green>✔ Nenhum problema detectado!");
        }
        
        MessageUtils.send(sender, "");
        
        // Recomendações
        if (!report.recommendations().isEmpty()) {
            MessageUtils.send(sender, "  <yellow>📋 Recomendações:");
            for (var rec : report.recommendations().stream().limit(3).toList()) {
                MessageUtils.send(sender, "    " + rec.priority().getColor() + "● " + rec.title());
            }
        }
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, "  <dark_gray>Use <yellow>/perf issues <dark_gray>para detalhes dos problemas");
        MessageUtils.send(sender, "");
    }

    // ========== DIAGNOSE ==========
    
    private void sendDiagnose(CommandSender sender) {
        if (!checkSpark(sender)) return;
        
        var diagnosis = SparkPerformanceManager.getInstance().diagnose();
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, PREFIX + "<white>🩺 Diagnóstico de Saúde");
        MessageUtils.send(sender, "");
        
        MessageUtils.send(sender, "  <gray>Status Geral: " + diagnosis.overallHealth().getColor() + 
            diagnosis.overallHealth().getIcon() + " " + diagnosis.overallHealth().getLabel());
        MessageUtils.send(sender, "");
        
        // Cada componente
        sendDiagnosisLine(sender, "TPS", diagnosis.tps());
        sendDiagnosisLine(sender, "MSPT", diagnosis.mspt());
        sendDiagnosisLine(sender, "CPU", diagnosis.cpu());
        sendDiagnosisLine(sender, "Memória", diagnosis.memory());
        sendDiagnosisLine(sender, "GC", diagnosis.gc());
        
        MessageUtils.send(sender, "");
    }

    private void sendDiagnosisLine(CommandSender sender, String name, HealthIssue issue) {
        MessageUtils.send(sender, "  " + issue.getColor() + issue.getIcon() + " <gray>" + name + ": " + 
            issue.getColor() + issue.message());
    }

    // ========== ISSUES ==========
    
    private void sendIssues(CommandSender sender) {
        var report = PerformanceReport.generateFullReport();
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, PREFIX + "<white>⚠ Problemas Detectados");
        MessageUtils.send(sender, "");
        
        if (report.issues().isEmpty()) {
            MessageUtils.send(sender, "  <green>✔ Nenhum problema detectado!");
            MessageUtils.send(sender, "");
            return;
        }
        
        for (var issue : report.issues()) {
            MessageUtils.send(sender, "  " + issue.level().getColor() + issue.category().getIcon() + 
                " <bold>" + issue.title() + "</bold>");
            MessageUtils.send(sender, "    <gray>" + issue.description());
            MessageUtils.send(sender, "    <dark_gray>💡 " + issue.suggestion());
            MessageUtils.send(sender, "");
        }
    }

    // ========== WATCHER ==========
    
    private void sendWatcher(CommandSender sender) {
        var watcher = module.getHealthWatcher();
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, PREFIX + "<white>👁 Health Watcher");
        MessageUtils.send(sender, "");
        
        if (watcher == null) {
            MessageUtils.send(sender, "  <red>Watcher não inicializado");
            return;
        }
        
        MessageUtils.send(sender, "  <gray>Checks realizados: <white>" + watcher.getChecksPerformed());
        MessageUtils.send(sender, "  <gray>Alertas disparados: <yellow>" + watcher.getAlertsTriggered());
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, "  <gray>Alertas ativos:");
        MessageUtils.send(sender, "    <gray>TPS: " + (watcher.isTpsAlertActive() ? "<red>⚠ ATIVO" : "<green>OK"));
        MessageUtils.send(sender, "    <gray>Memória: " + (watcher.isMemoryAlertActive() ? "<red>⚠ ATIVO" : "<green>OK"));
        MessageUtils.send(sender, "    <gray>CPU: " + (watcher.isCpuAlertActive() ? "<red>⚠ ATIVO" : "<green>OK"));
        MessageUtils.send(sender, "");
    }

    // ========== CLEAR ==========
    
    private void clearStats(CommandSender sender) {
        MidgardProfiler.clearStats();
        var analyzer = MidgardAnalyzer.getInstance();
        if (analyzer != null) {
            analyzer.clearTracking();
        }
        
        MessageUtils.send(sender, PREFIX + "<green>✔ Todas as estatísticas foram limpas.");
    }

    // ========== HELP ==========
    
    private void sendHelp(CommandSender sender) {
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, HEADER);
        MessageUtils.send(sender, "<gradient:#ff6b6b:#feca57><bold>         ⚡ PERFORMANCE HELP ⚡</bold></gradient>");
        MessageUtils.send(sender, HEADER);
        MessageUtils.send(sender, "");
        
        // Categoria: Interface Gráfica
        MessageUtils.send(sender, "  <gradient:#ff6b6b:#feca57>▸ Interface Gráfica</gradient>");
        MessageUtils.send(sender, "    <yellow>/perf gui <dark_gray>.............. <gray>✨ Abre dashboard interativo");
        MessageUtils.send(sender, "");
        
        // Categoria: Dashboard
        MessageUtils.send(sender, "  <gradient:#ff6b6b:#feca57>▸ Dashboard</gradient>");
        MessageUtils.send(sender, "    <yellow>/perf <dark_gray>................... <gray>Visão geral do servidor");
        MessageUtils.send(sender, "");
        
        // Categoria: Métricas Spark
        MessageUtils.send(sender, "  <gradient:#ff6b6b:#feca57>▸ Métricas Spark</gradient>");
        MessageUtils.send(sender, "    <yellow>/perf tps <dark_gray>............... <gray>TPS (1m, 5m, 15m)");
        MessageUtils.send(sender, "    <yellow>/perf mspt <dark_gray>.............. <gray>Milliseconds Per Tick");
        MessageUtils.send(sender, "    <yellow>/perf memory <dark_gray>............ <gray>Heap usado/total/max");
        MessageUtils.send(sender, "    <yellow>/perf cpu <dark_gray>............... <gray>CPU processo/sistema");
        MessageUtils.send(sender, "    <yellow>/perf gc <dark_gray>................ <gray>Garbage Collection stats");
        MessageUtils.send(sender, "");
        
        // Categoria: Análise Midgard
        MessageUtils.send(sender, "  <gradient:#ff6b6b:#feca57>▸ Análise Midgard</gradient>");
        MessageUtils.send(sender, "    <yellow>/perf modules <dark_gray>........... <gray>Status de cada módulo");
        MessageUtils.send(sender, "    <yellow>/perf events <dark_gray>............ <gray>Listeners registrados");
        MessageUtils.send(sender, "    <yellow>/perf commands <dark_gray>.......... <gray>Profiling de comandos");
        MessageUtils.send(sender, "    <yellow>/perf profiler <dark_gray>.......... <gray>Top operações lentas");
        MessageUtils.send(sender, "");
        
        // Categoria: Diagnóstico
        MessageUtils.send(sender, "  <gradient:#ff6b6b:#feca57>▸ Diagnóstico</gradient>");
        MessageUtils.send(sender, "    <yellow>/perf report <dark_gray>............ <gray>Relatório com score/grade");
        MessageUtils.send(sender, "    <yellow>/perf diagnose <dark_gray>.......... <gray>Diagnóstico de saúde");
        MessageUtils.send(sender, "    <yellow>/perf issues <dark_gray>............ <gray>Lista problemas detectados");
        MessageUtils.send(sender, "");
        
        // Categoria: Utilitários
        MessageUtils.send(sender, "  <gradient:#ff6b6b:#feca57>▸ Utilitários</gradient>");
        MessageUtils.send(sender, "    <yellow>/perf watcher <dark_gray>........... <gray>Health watcher status");
        MessageUtils.send(sender, "    <yellow>/perf clear <dark_gray>............. <gray>Limpar estatísticas");
        MessageUtils.send(sender, "    <yellow>/perf help <dark_gray>.............. <gray>Esta mensagem");
        MessageUtils.send(sender, "");
        
        // Nota sobre Spark
        if (!SparkPerformanceManager.isAvailable()) {
            MessageUtils.send(sender, "  <red>⚠ Spark não detectado! Algumas métricas indisponíveis.");
            MessageUtils.send(sender, "  <gray>Baixe em: <aqua><click:open_url:'https://spark.lucko.me/'>spark.lucko.me</click>");
            MessageUtils.send(sender, "");
        }
        
        MessageUtils.send(sender, HEADER);
    }

    // ========== HELPERS ==========
    
    private boolean checkSpark(CommandSender sender) {
        if (!SparkPerformanceManager.isAvailable()) {
            MessageUtils.send(sender, "");
            MessageUtils.send(sender, PREFIX + "<red>✘ Spark não está instalado!");
            MessageUtils.send(sender, "");
            MessageUtils.send(sender, "  <gray>O módulo de Performance requer o Spark Profiler");
            MessageUtils.send(sender, "  <gray>para fornecer métricas precisas do servidor.");
            MessageUtils.send(sender, "");
            MessageUtils.send(sender, "  <aqua>Download: <white><click:open_url:'https://spark.lucko.me/'>https://spark.lucko.me/</click>");
            MessageUtils.send(sender, "");
            return false;
        }
        return true;
    }

    private void sendTPSBar(CommandSender sender, double tps) {
        int filled = (int) (tps / 20.0 * 20);
        StringBuilder bar = new StringBuilder("    <dark_gray>[");
        for (int i = 0; i < 20; i++) {
            if (i < filled) {
                if (tps >= 19) bar.append("<green>");
                else if (tps >= 17) bar.append("<yellow>");
                else if (tps >= 15) bar.append("<gold>");
                else bar.append("<red>");
                bar.append("█");
            } else {
                bar.append("<dark_gray>░");
            }
        }
        bar.append("<dark_gray>]");
        MessageUtils.send(sender, bar.toString());
    }

    private void sendMemoryBar(CommandSender sender, double percent) {
        int filled = (int) (percent / 100.0 * 20);
        StringBuilder bar = new StringBuilder("    <dark_gray>[");
        for (int i = 0; i < 20; i++) {
            if (i < filled) {
                if (percent <= 60) bar.append("<green>");
                else if (percent <= 75) bar.append("<yellow>");
                else if (percent <= 85) bar.append("<gold>");
                else bar.append("<red>");
                bar.append("█");
            } else {
                bar.append("<dark_gray>░");
            }
        }
        bar.append("<dark_gray>] ");
        
        String color = percent <= 60 ? "<green>" : (percent <= 75 ? "<yellow>" : (percent <= 85 ? "<gold>" : "<red>"));
        bar.append(color).append(String.format("%.1f%%", percent));
        MessageUtils.send(sender, bar.toString());
    }

    private String getTimeColor(long ms) {
        if (ms < 100) return "<green>";
        if (ms < 300) return "<yellow>";
        if (ms < 500) return "<gold>";
        return "<red>";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> subcommands = List.of(
                // Interface Gráfica
                "gui", "menu",
                // Dashboard
                "help",
                // Métricas Spark
                "tps", "mspt", "memory", "mem", "cpu", "gc",
                // Análise Midgard
                "modules", "mods", "events", "commands", "cmds", "profiler", "profile",
                // Diagnóstico
                "report", "diagnose", "diag", "issues",
                // Utilitários
                "watcher", "clear"
            );
            return subcommands.stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }
}

