package me.ray.midgard.modules.performance.spark;

import me.ray.midgard.modules.performance.spark.MidgardAnalyzer.*;
import me.ray.midgard.modules.performance.spark.SparkPerformanceManager.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Gerador de relatórios de performance detalhados.
 * Combina dados do Spark com análise do MidgardRPG.
 */
public class PerformanceReport {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Gera relatório completo de performance.
     */
    public static FullReport generateFullReport() {
        SparkPerformanceManager sparkManager = SparkPerformanceManager.getInstance();
        MidgardAnalyzer analyzer = MidgardAnalyzer.getInstance();

        ServerMetrics metrics = sparkManager != null ? sparkManager.getMetrics() : ServerMetrics.unavailable();
        HealthDiagnosis diagnosis = sparkManager != null ? sparkManager.diagnose() : null;
        AnalysisSnapshot analysis = analyzer != null ? analyzer.analyze() : null;

        List<Issue> issues = collectIssues(metrics, diagnosis, analysis);
        List<Recommendation> recommendations = generateRecommendations(issues, metrics, analysis);

        return new FullReport(
            LocalDateTime.now().format(TIMESTAMP_FORMAT),
            SparkPerformanceManager.isAvailable(),
            metrics,
            diagnosis,
            analysis,
            issues,
            recommendations,
            calculateOverallScore(metrics, diagnosis, issues)
        );
    }

    /**
     * Gera relatório rápido (apenas métricas essenciais).
     */
    public static QuickReport generateQuickReport() {
        SparkPerformanceManager sparkManager = SparkPerformanceManager.getInstance();
        
        if (sparkManager == null || !SparkPerformanceManager.isAvailable()) {
            return QuickReport.unavailable();
        }

        ServerMetrics metrics = sparkManager.getMetrics();
        HealthDiagnosis diagnosis = sparkManager.diagnose();

        return new QuickReport(
            metrics.tps().last5s(),
            metrics.mspt().available() ? metrics.mspt().last10s().median() : -1,
            metrics.memory().usedPercent(),
            metrics.cpu().available() ? metrics.cpu().process().seconds10() * 100 : -1,
            diagnosis.overallHealth(),
            countCriticalIssues(diagnosis),
            LocalDateTime.now().format(TIMESTAMP_FORMAT)
        );
    }

    // ========== COLETA DE PROBLEMAS ==========

    private static List<Issue> collectIssues(ServerMetrics metrics, HealthDiagnosis diagnosis, AnalysisSnapshot analysis) {
        List<Issue> issues = new ArrayList<>();

        // Issues de métricas do servidor
        if (diagnosis != null) {
            if (diagnosis.tps().level().ordinal() >= HealthLevel.WARNING.ordinal()) {
                issues.add(new Issue(
                    IssueCategory.TPS,
                    diagnosis.tps().level(),
                    "TPS Baixo",
                    diagnosis.tps().message(),
                    "Verifique entidades, redstone, hoppers e plugins pesados"
                ));
            }

            if (diagnosis.mspt().level().ordinal() >= HealthLevel.WARNING.ordinal()) {
                issues.add(new Issue(
                    IssueCategory.MSPT,
                    diagnosis.mspt().level(),
                    "MSPT Alto",
                    diagnosis.mspt().message(),
                    "Ticks estão demorando demais - identifique operações síncronas pesadas"
                ));
            }

            if (diagnosis.memory().level().ordinal() >= HealthLevel.WARNING.ordinal()) {
                issues.add(new Issue(
                    IssueCategory.MEMORY,
                    diagnosis.memory().level(),
                    "Memória Alta",
                    diagnosis.memory().message(),
                    "Considere aumentar heap ou verificar memory leaks"
                ));
            }

            if (diagnosis.cpu().level().ordinal() >= HealthLevel.WARNING.ordinal()) {
                issues.add(new Issue(
                    IssueCategory.CPU,
                    diagnosis.cpu().level(),
                    "CPU Alta",
                    diagnosis.cpu().message(),
                    "Identifique processos intensivos ou loops infinitos"
                ));
            }

            if (diagnosis.gc().level().ordinal() >= HealthLevel.WARNING.ordinal()) {
                issues.add(new Issue(
                    IssueCategory.GC,
                    diagnosis.gc().level(),
                    "GC Frequente",
                    diagnosis.gc().message(),
                    "Muitas alocações de objetos - otimize uso de memória"
                ));
            }
        }

        // Issues de análise do MidgardRPG
        if (analysis != null) {
            // Módulos lentos
            for (ModuleAnalysis module : analysis.modules()) {
                if (module.enableTime() > 1000) {
                    issues.add(new Issue(
                        IssueCategory.MODULE,
                        HealthLevel.CRITICAL,
                        "Módulo Lento: " + module.name(),
                        String.format("Tempo de inicialização: %dms", module.enableTime()),
                        "Otimize onEnable() ou mova operações para async"
                    ));
                } else if (module.enableTime() > 500) {
                    issues.add(new Issue(
                        IssueCategory.MODULE,
                        HealthLevel.WARNING,
                        "Módulo Moderado: " + module.name(),
                        String.format("Tempo de inicialização: %dms", module.enableTime()),
                        "Considere lazy loading ou async init"
                    ));
                }

                // Operações críticas dentro do módulo
                for (OperationAnalysis op : module.operations()) {
                    if (op.severity() == Severity.CRITICAL || op.severity() == Severity.SEVERE) {
                        issues.add(new Issue(
                            IssueCategory.OPERATION,
                            op.severity() == Severity.SEVERE ? HealthLevel.SEVERE : HealthLevel.CRITICAL,
                            "Operação Lenta: " + op.name(),
                            String.format("Tempo máximo: %dms (%d execuções)", op.maxTime(), op.count()),
                            "Otimize ou mova para thread async"
                        ));
                    }
                }
            }

            // Eventos lentos
            for (RegisteredListenerInfo listener : analysis.events().slowest()) {
                if (listener.maxTime() > 50) {
                    issues.add(new Issue(
                        IssueCategory.EVENT,
                        HealthLevel.CRITICAL,
                        "Listener Lento: " + listener.eventName(),
                        String.format("%s - %dms máximo", listener.listenerClass(), listener.maxTime()),
                        "Mova lógica pesada para async ou otimize"
                    ));
                }
            }

            // Comandos lentos
            for (CommandInfo cmd : analysis.commands().slowest()) {
                if (cmd.maxTime() > 100) {
                    issues.add(new Issue(
                        IssueCategory.COMMAND,
                        HealthLevel.WARNING,
                        "Comando Lento: /" + cmd.name(),
                        String.format("Tempo máximo: %dms", cmd.maxTime()),
                        "Comandos devem executar rapidamente - use async para operações pesadas"
                    ));
                }
            }
        }

        // Ordena por severidade
        issues.sort((a, b) -> b.level().ordinal() - a.level().ordinal());
        return issues;
    }

    // ========== RECOMENDAÇÕES ==========

    private static List<Recommendation> generateRecommendations(List<Issue> issues, ServerMetrics metrics, AnalysisSnapshot analysis) {
        List<Recommendation> recommendations = new ArrayList<>();

        // Recomendações baseadas em issues
        boolean hasTPSIssue = issues.stream().anyMatch(i -> i.category() == IssueCategory.TPS);
        boolean hasMemoryIssue = issues.stream().anyMatch(i -> i.category() == IssueCategory.MEMORY);
        boolean hasGCIssue = issues.stream().anyMatch(i -> i.category() == IssueCategory.GC);
        boolean hasModuleIssue = issues.stream().anyMatch(i -> i.category() == IssueCategory.MODULE);

        if (hasTPSIssue) {
            recommendations.add(new Recommendation(
                RecommendationPriority.HIGH,
                "Otimização de TPS",
                List.of(
                    "Execute /spark profiler para identificar gargalos",
                    "Verifique entidades excessivas em chunks",
                    "Reduza hoppers e redstone complexa",
                    "Use Paper's async chunk loading"
                )
            ));
        }

        if (hasMemoryIssue || hasGCIssue) {
            recommendations.add(new Recommendation(
                RecommendationPriority.HIGH,
                "Otimização de Memória",
                List.of(
                    "Execute /spark heapsummary para análise de heap",
                    "Verifique cache de dados não liberados",
                    "Use object pooling para objetos frequentes",
                    "Considere aumentar -Xmx se necessário"
                )
            ));
        }

        if (hasModuleIssue) {
            recommendations.add(new Recommendation(
                RecommendationPriority.MEDIUM,
                "Otimização de Módulos",
                List.of(
                    "Mova I/O para CompletableFuture",
                    "Implemente lazy loading para recursos pesados",
                    "Cache resultados de operações frequentes",
                    "Use BukkitScheduler para tarefas não-críticas"
                )
            ));
        }

        // Recomendações gerais baseadas em análise
        if (analysis != null) {
            if (analysis.events().totalListeners() > 50) {
                recommendations.add(new Recommendation(
                    RecommendationPriority.LOW,
                    "Muitos Event Listeners",
                    List.of(
                        "Considere combinar listeners relacionados",
                        "Use event priorities corretamente",
                        "Evite listeners em eventos de alta frequência"
                    )
                ));
            }

            if (analysis.profiler().trackedOperations() > 100) {
                recommendations.add(new Recommendation(
                    RecommendationPriority.LOW,
                    "Muitas Operações Rastreadas",
                    List.of(
                        "Limite uso do profiler em produção",
                        "Use sampling ao invés de tracing completo"
                    )
                ));
            }
        }

        // Recomendação de Spark se não disponível
        if (!SparkPerformanceManager.isAvailable()) {
            recommendations.add(new Recommendation(
                RecommendationPriority.HIGH,
                "Instale o Spark Profiler",
                List.of(
                    "Spark fornece métricas precisas de TPS/MSPT/CPU",
                    "Permite profiling detalhado de threads",
                    "Análise de heap e GC em tempo real",
                    "Download: https://spark.lucko.me/"
                )
            ));
        }

        recommendations.sort((a, b) -> a.priority().ordinal() - b.priority().ordinal());
        return recommendations;
    }

    // ========== HELPERS ==========

    private static int countCriticalIssues(HealthDiagnosis diagnosis) {
        if (diagnosis == null) return 0;
        
        int count = 0;
        if (diagnosis.tps().level().ordinal() >= HealthLevel.CRITICAL.ordinal()) count++;
        if (diagnosis.mspt().level().ordinal() >= HealthLevel.CRITICAL.ordinal()) count++;
        if (diagnosis.memory().level().ordinal() >= HealthLevel.CRITICAL.ordinal()) count++;
        if (diagnosis.cpu().level().ordinal() >= HealthLevel.CRITICAL.ordinal()) count++;
        if (diagnosis.gc().level().ordinal() >= HealthLevel.CRITICAL.ordinal()) count++;
        return count;
    }

    private static int calculateOverallScore(ServerMetrics metrics, HealthDiagnosis diagnosis, List<Issue> issues) {
        if (!metrics.available() || diagnosis == null) return 0;

        int score = 100;

        // Penalidades por issues
        for (Issue issue : issues) {
            switch (issue.level()) {
                case SEVERE -> score -= 20;
                case CRITICAL -> score -= 15;
                case WARNING -> score -= 8;
                case GOOD -> score -= 3;
                default -> {}
            }
        }

        // Bônus por saúde geral
        switch (diagnosis.overallHealth()) {
            case EXCELLENT -> score += 10;
            case GOOD -> score += 5;
            default -> {}
        }

        return Math.max(0, Math.min(100, score));
    }

    // ========== RECORDS ==========

    public record FullReport(
        String timestamp,
        boolean sparkAvailable,
        ServerMetrics metrics,
        HealthDiagnosis diagnosis,
        AnalysisSnapshot analysis,
        List<Issue> issues,
        List<Recommendation> recommendations,
        int overallScore
    ) {
        public String getScoreColor() {
            if (overallScore >= 90) return "<green>";
            if (overallScore >= 75) return "<yellow>";
            if (overallScore >= 50) return "<gold>";
            return "<red>";
        }

        public String getScoreGrade() {
            if (overallScore >= 95) return "S+";
            if (overallScore >= 90) return "S";
            if (overallScore >= 85) return "A+";
            if (overallScore >= 80) return "A";
            if (overallScore >= 75) return "B+";
            if (overallScore >= 70) return "B";
            if (overallScore >= 60) return "C";
            if (overallScore >= 50) return "D";
            return "F";
        }
    }

    public record QuickReport(
        double tps,
        double mspt,
        double memoryPercent,
        double cpuPercent,
        HealthLevel health,
        int criticalIssues,
        String timestamp
    ) {
        public static QuickReport unavailable() {
            return new QuickReport(-1, -1, -1, -1, HealthLevel.UNKNOWN, 0, 
                LocalDateTime.now().format(TIMESTAMP_FORMAT));
        }

        public boolean isAvailable() {
            return tps >= 0;
        }
    }

    public record Issue(
        IssueCategory category,
        HealthLevel level,
        String title,
        String description,
        String suggestion
    ) {}

    public record Recommendation(
        RecommendationPriority priority,
        String title,
        List<String> steps
    ) {}

    public enum IssueCategory {
        TPS("⏱", "TPS"),
        MSPT("⚡", "MSPT"),
        MEMORY("💾", "Memória"),
        CPU("💻", "CPU"),
        GC("🗑", "GC"),
        MODULE("📦", "Módulo"),
        EVENT("📡", "Evento"),
        COMMAND("⌨", "Comando"),
        OPERATION("⚙", "Operação");

        private final String icon;
        private final String label;

        IssueCategory(String icon, String label) {
            this.icon = icon;
            this.label = label;
        }

        public String getIcon() { return icon; }
        public String getLabel() { return label; }
    }

    public enum RecommendationPriority {
        HIGH("<red>", "Alta"),
        MEDIUM("<yellow>", "Média"),
        LOW("<gray>", "Baixa");

        private final String color;
        private final String label;

        RecommendationPriority(String color, String label) {
            this.color = color;
            this.label = label;
        }

        public String getColor() { return color; }
        public String getLabel() { return label; }
    }
}
