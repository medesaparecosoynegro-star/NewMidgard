package me.ray.midgard.core;

import me.ray.midgard.core.debug.DebugCategory;
import me.ray.midgard.core.debug.MidgardLogger;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;

/**
 * Gerencia o ciclo de vida dos módulos do RPG.
 */
public class ModuleManager {

    private final JavaPlugin plugin;
    private final Map<String, RPGModule> modules = new HashMap<>();

    /**
     * Construtor do ModuleManager.
     *
     * @param plugin Instância do plugin principal.
     */
    public ModuleManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Registra um novo módulo.
     *
     * @param module Módulo a ser registrado.
     */
    public void registerModule(RPGModule module) {
        if (module == null) {
            MidgardLogger.error("Tentativa de registrar um módulo nulo!");
            return;
        }
        if (modules.containsKey(module.getName())) {
            MidgardLogger.warn("Módulo já registrado: " + module.getName() + ". Ignorando duplicata.");
            return;
        }
        modules.put(module.getName(), module);
    }

    /**
     * Obtém todos os módulos registrados.
     */
    public Map<String, RPGModule> getModules() {
        return java.util.Collections.unmodifiableMap(modules);
    }

    /**
     * Habilita todos os módulos registrados.
     */
    public void enableAll() {
        modules.values().stream()
                .filter(java.util.Objects::nonNull)
                .sorted((m1, m2) -> Integer.compare(m2.getPriority().getValue(), m1.getPriority().getValue()))
                .forEach(module -> {
                    try {
                        me.ray.midgard.core.debug.MidgardProfiler.monitor("module_enable:" + module.getName(), () -> {
                            MidgardLogger.info("Habilitando módulo: " + module.getName());
                            MidgardLogger.debug(DebugCategory.CORE, "Iniciando módulo %s (Prioridade: %s)", module.getName(), module.getPriority());
                            module.onEnable(plugin);
                            module.setEnabled(true);
                        });
                    } catch (Throwable e) {
                        MidgardLogger.error("Falha crítica ao habilitar módulo: " + module.getName(), e);
                        module.setEnabled(false);
                    }
                });
    }

    /**
     * Recarrega a configuração de todos os módulos.
     */
    public void reloadAll() {
        modules.values().forEach(module -> {
            try {
                if (module != null && module.isEnabled()) {
                    module.reloadConfig();
                    MidgardLogger.info("Módulo recarregado: " + module.getName());
                }
            } catch (Exception e) {
                MidgardLogger.error("Falha ao recarregar módulo: " + (module != null ? module.getName() : "null"), e);
            }
        });
    }

    /**
     * Obtém um módulo pelo nome.
     */
    public RPGModule getModule(String name) {
        return modules.get(name);
    }

    /**
     * Desabilita todos os módulos registrados.
     */
    public void disableAll() {
        for (RPGModule module : modules.values()) {
            if (module != null && module.isEnabled()) {
                try {
                    MidgardLogger.info("Desabilitando módulo: " + module.getName());
                    module.onDisable(plugin);
                    module.setEnabled(false);
                } catch (Throwable e) {
                    MidgardLogger.error("Falha ao desabilitar módulo: " + module.getName(), e);
                }
            }
        }
    }
}
