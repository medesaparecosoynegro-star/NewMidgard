package me.ray.midgard.modules.performance;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.RPGModule;
import me.ray.midgard.core.debug.MidgardLogger;
import me.ray.midgard.modules.performance.spark.MidgardAnalyzer;
import me.ray.midgard.modules.performance.spark.SparkPerformanceManager;
import org.bukkit.Bukkit;

/**
 * Módulo de Performance do MidgardRPG.
 * 
 * Utiliza Spark como fonte primária de métricas e fornece:
 * - Monitoramento em tempo real (TPS, MSPT, CPU, Memória, GC)
 * - Análise profunda do projeto MidgardRPG
 * - Diagnóstico automático de problemas
 * - Relatórios detalhados com recomendações
 * 
 * @requires Spark Profiler (https://spark.lucko.me/)
 */
public class PerformanceModule extends RPGModule {

    private static PerformanceModule instance;
    
    // Monitor de saúde contínuo
    private HealthWatcher healthWatcher;
    private int watcherTaskId = -1;

    public PerformanceModule() {
        super("Performance");
    }

    @Override
    public void onEnable() {
        instance = this;
        
        // Inicializa gerenciador Spark com delay via scheduler para resolver race-condition
        Bukkit.getScheduler().runTask(MidgardCore.getInstance(), () -> {
            SparkPerformanceManager.init();
            MidgardAnalyzer.init();
            
            // Log de status após init tardio
            if (SparkPerformanceManager.isAvailable()) {
                MidgardLogger.info("[Performance] ✔ Módulo ativado com integração Spark completa");
            } else {
                MidgardLogger.warn("[Performance] ⚠ Módulo funcionando em modo limitado (sem Spark)");
            }
        });
        
        // Inicia monitoramento contínuo de saúde
        startHealthWatcher();
        
        // Registra comando apenas no AdminCommand para /rpg admin performance
        PerformanceCommand perfCmd = new PerformanceCommand(this);
        if (MidgardCore.getAdminCommand() != null) {
            MidgardCore.getAdminCommand().registerSubcommand(perfCmd);
        }
        
        // Registra comando direto /performance e /perf
        var perfBukkitCmd = getPlugin().getCommand("performance");
        if (perfBukkitCmd != null) {
            perfBukkitCmd.setExecutor(perfCmd);
            perfBukkitCmd.setTabCompleter(perfCmd);
        }
        
        MidgardLogger.info("Módulo Performance inicializado (processo em background)");
    }

    @Override
    public void onDisable() {
        stopHealthWatcher();
        
        MidgardLogger.info("[Performance] Módulo desativado");
        instance = null;
    }

    /**
     * Inicia o monitoramento contínuo de saúde do servidor.
     * Verifica a cada 10 segundos e alerta sobre problemas críticos.
     */
    private void startHealthWatcher() {
        if (watcherTaskId != -1) return;
        
        healthWatcher = new HealthWatcher(this);
        watcherTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(
            getPlugin(), healthWatcher, 200L, 200L // 10 segundos
        ).getTaskId();
    }

    private void stopHealthWatcher() {
        if (watcherTaskId != -1) {
            Bukkit.getScheduler().cancelTask(watcherTaskId);
            watcherTaskId = -1;
        }
    }

    public static PerformanceModule getInstance() {
        return instance;
    }

    public HealthWatcher getHealthWatcher() {
        return healthWatcher;
    }

    // ========== HEALTH WATCHER ==========

    /**
     * Monitora saúde do servidor em background e dispara alertas.
     */
    public static class HealthWatcher implements Runnable {
        
        private final PerformanceModule module;
        
        // Estado de alertas (evita spam)
        private boolean tpsAlertActive = false;
        private boolean memoryAlertActive = false;
        private boolean cpuAlertActive = false;
        
        // Thresholds
        private static final double TPS_CRITICAL = 15.0;
        private static final double TPS_WARNING = 17.0;
        private static final double MEMORY_CRITICAL = 90.0;
        private static final double CPU_CRITICAL = 85.0;
        
        // Estatísticas
        private long checksPerformed = 0;
        private long alertsTriggered = 0;

        public HealthWatcher(PerformanceModule module) {
            this.module = module;
        }

        @Override
        public void run() {
            if (!SparkPerformanceManager.isAvailable()) return;
            
            checksPerformed++;
            var manager = SparkPerformanceManager.getInstance();
            var metrics = manager.getMetrics();
            
            checkTPS(metrics.tps());
            checkMemory(metrics.memory());
            checkCPU(metrics.cpu());
        }

        private void checkTPS(SparkPerformanceManager.TPSMetrics tps) {
            if (!tps.available()) return;
            
            double current = tps.last5s();
            
            if (current < TPS_CRITICAL) {
                if (!tpsAlertActive) {
                    tpsAlertActive = true;
                    alertsTriggered++;
                    MidgardLogger.warn(String.format("[Performance] ⚠ TPS CRÍTICO: %.1f - Servidor com lag severo!", current));
                    broadcastToAdmins("<red>⚠ <gray>TPS CRÍTICO: <red>%.1f", current);
                }
            } else if (current < TPS_WARNING) {
                if (!tpsAlertActive) {
                    tpsAlertActive = true;
                    alertsTriggered++;
                    MidgardLogger.warn(String.format("[Performance] ⚠ TPS baixo: %.1f", current));
                }
            } else {
                if (tpsAlertActive) {
                    tpsAlertActive = false;
                    MidgardLogger.info(String.format("[Performance] ✔ TPS normalizado: %.1f", current));
                }
            }
        }

        private void checkMemory(SparkPerformanceManager.MemoryMetrics mem) {
            if (!mem.available()) return;
            
            double percent = mem.usedPercent();
            
            if (percent > MEMORY_CRITICAL) {
                if (!memoryAlertActive) {
                    memoryAlertActive = true;
                    alertsTriggered++;
                    MidgardLogger.warn(String.format("[Performance] ⚠ MEMÓRIA CRÍTICA: %.1f%% - OOM iminente!", percent));
                    broadcastToAdmins("<red>⚠ <gray>MEMÓRIA CRÍTICA: <red>%.1f%%", percent);
                }
            } else {
                if (memoryAlertActive) {
                    memoryAlertActive = false;
                    MidgardLogger.info(String.format("[Performance] ✔ Memória normalizada: %.1f%%", percent));
                }
            }
        }

        private void checkCPU(SparkPerformanceManager.CPUMetrics cpu) {
            if (!cpu.available()) return;
            
            double percent = cpu.process().seconds10() * 100;
            
            if (percent > CPU_CRITICAL) {
                if (!cpuAlertActive) {
                    cpuAlertActive = true;
                    alertsTriggered++;
                    MidgardLogger.warn(String.format("[Performance] ⚠ CPU alta: %.1f%%", percent));
                }
            } else {
                if (cpuAlertActive) {
                    cpuAlertActive = false;
                }
            }
        }

        private void broadcastToAdmins(String message, Object... args) {
            String formatted = String.format(message, args);
            Bukkit.getScheduler().runTask(module.getPlugin(), () -> {
                Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("midgard.admin"))
                    .forEach(p -> me.ray.midgard.core.text.MessageUtils.send(p, formatted));
            });
        }

        public long getChecksPerformed() { return checksPerformed; }
        public long getAlertsTriggered() { return alertsTriggered; }
        public boolean isTpsAlertActive() { return tpsAlertActive; }
        public boolean isMemoryAlertActive() { return memoryAlertActive; }
        public boolean isCpuAlertActive() { return cpuAlertActive; }
    }
}

