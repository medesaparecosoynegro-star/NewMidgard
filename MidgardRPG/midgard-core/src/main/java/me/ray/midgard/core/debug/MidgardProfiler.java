package me.ray.midgard.core.debug;

import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import org.bukkit.Bukkit;

/**
 * Utilitário para integração com o Spark Profiler.
 * Permite criar métricas customizadas que aparecem nos relatórios do Spark.
 */
public class MidgardProfiler {

    @SuppressWarnings("unused")
    private static Spark spark;
    private static boolean enabled = false;

    // private static java.util.Map<String, me.lucko.spark.api.statistic.CustomStatistic<Long>> stats = new java.util.concurrent.ConcurrentHashMap<>();

    public static void init() {
        try {
            // Verifica se o Spark está presente
            if (Bukkit.getPluginManager().isPluginEnabled("spark")) {
                spark = SparkProvider.get();
                enabled = true;
                MidgardLogger.info("Integração com Spark Profiler ativada.");
            }
        } catch (Throwable e) {
            MidgardLogger.warn("Spark detectado mas falha ao integrar: " + e.getMessage());
            enabled = false;
        }
    }

    /**
     * Executes a runnable and tracks its execution time in Spark.
     * @param name Unique name for the statistic (e.g. "module_load:midgard-combat")
     * @param task Logic to execute
     */
    public static void monitor(String name, Runnable task) {
        if (!enabled) {
            task.run();
            return;
        }
        long start = System.nanoTime();
        try {
            task.run();
        } finally {
            long duration = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            record(name, duration);
        }
    }

    /**
     * Executes a supplier and tracks its execution time in Spark.
     * @param name Unique name for the statistic
     * @param supplier Logic to execute returning a value
     * @return Value returned by logic
     */
    public static <T> T monitor(String name, java.util.function.Supplier<T> supplier) {
        if (!enabled) {
            return supplier.get();
        }
        long start = System.nanoTime();
        try {
            return supplier.get();
        } finally {
            long duration = java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            record(name, duration);
        }
    }

    private static final java.util.Map<String, Long> maxExecutionTimes = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<String, Long> lastExecutionTimes = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<String, Integer> invocationCounts = new java.util.concurrent.ConcurrentHashMap<>();

    private static void record(String name, long millis) {
        // Store stats
        lastExecutionTimes.put(name, millis);
        maxExecutionTimes.merge(name, millis, Math::max);
        invocationCounts.merge(name, 1, Integer::sum);

        // Log if it's unusually slow (>50ms)
        if (millis > 50) {
            MidgardLogger.debug(me.ray.midgard.core.debug.DebugCategory.CORE,
                "[Profiler] %s took %d ms", name, millis);
        }
    }

    public static java.util.Map<String, Long> getMaxExecutionTimes() {
        return java.util.Collections.unmodifiableMap(maxExecutionTimes);
    }

    public static java.util.Map<String, Long> getLastExecutionTimes() {
        return java.util.Collections.unmodifiableMap(lastExecutionTimes);
    }

    public static java.util.Map<String, Integer> getInvocationCounts() {
        return java.util.Collections.unmodifiableMap(invocationCounts);
    }

    public static void clearStats() {
        maxExecutionTimes.clear();
        lastExecutionTimes.clear();
        invocationCounts.clear();
    }
    
    public static boolean isEnabled() {
        return enabled;
    }
}
