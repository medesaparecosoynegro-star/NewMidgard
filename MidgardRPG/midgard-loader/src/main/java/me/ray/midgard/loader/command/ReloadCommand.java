package me.ray.midgard.loader.command;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.RPGModule;
import me.ray.midgard.core.attribute.Attribute;
import me.ray.midgard.core.attribute.AttributeRegistry;
import me.ray.midgard.core.command.MidgardCommand;
import me.ray.midgard.core.config.ConfigWrapper;
import me.ray.midgard.core.debug.MidgardLogger;
import me.ray.midgard.core.text.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Comando de reload completo do MidgardRPG.
 * Recarrega configurações, atributos, mensagens e todos os módulos.
 */
public class ReloadCommand extends MidgardCommand {

    private final JavaPlugin plugin;
    
    // Subcomandos válidos
    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "all", "config", "messages", "attributes", "modules"
    );

    public ReloadCommand(JavaPlugin plugin) {
        super("reload", "midgard.admin.reload", false);
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            // Reload total
            reloadAll(sender);
            return;
        }
        
        String subcommand = args[0].toLowerCase();
        
        switch (subcommand) {
            case "all" -> reloadAll(sender);
            case "config" -> reloadConfig(sender);
            case "messages" -> reloadMessages(sender);
            case "attributes" -> reloadAttributes(sender);
            case "modules" -> {
                if (args.length > 1) {
                    reloadSpecificModule(sender, args[1]);
                } else {
                    reloadAllModules(sender);
                }
            }
            default -> {
                // Talvez seja um nome de módulo
                if (MidgardCore.getModuleManager() != null && 
                    MidgardCore.getModuleManager().getModule(subcommand) != null) {
                    reloadSpecificModule(sender, subcommand);
                } else {
                    MessageUtils.send(sender, "<red>✘ Subcomando desconhecido: <white>" + subcommand);
                    MessageUtils.send(sender, "<gray>Uso: <yellow>/midgard reload [all|config|messages|attributes|modules|<módulo>]");
                }
            }
        }
    }

    /**
     * Reload completo de todo o plugin
     */
    private void reloadAll(CommandSender sender) {
        long startTime = System.currentTimeMillis();
        int reloadedCount = 0;
        List<String> errors = new ArrayList<>();
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, "<gradient:#a855f7:#ec4899>⟳ Iniciando reload completo do MidgardRPG...</gradient>");
        MessageUtils.send(sender, "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // 1. Reload Config Principal
        try {
            plugin.reloadConfig();
            MessageUtils.send(sender, "<green>  ✔ <gray>Configuração principal recarregada");
            reloadedCount++;
        } catch (Exception e) {
            errors.add("Config: " + e.getMessage());
            MessageUtils.send(sender, "<red>  ✘ <gray>Erro ao recarregar configuração principal");
            MidgardLogger.error("Erro ao recarregar config principal", e);
        }
        
        // 2. Reload Atributos
        try {
            int attrCount = reloadAttributesInternal();
            MessageUtils.send(sender, "<green>  ✔ <gray>Atributos recarregados <dark_gray>(" + attrCount + " atributos)");
            reloadedCount++;
        } catch (Exception e) {
            errors.add("Attributes: " + e.getMessage());
            MessageUtils.send(sender, "<red>  ✘ <gray>Erro ao recarregar atributos");
            MidgardLogger.error("Erro ao recarregar atributos", e);
        }
        
        // 3. Reload Mensagens/Idioma
        try {
            if (MidgardCore.getLanguageManager() != null) {
                String locale = plugin.getConfig().getString("settings.locale", "pt-br");
                MidgardCore.getLanguageManager().load(locale);
                MessageUtils.send(sender, "<green>  ✔ <gray>Mensagens recarregadas <dark_gray>(locale: " + locale + ")");
                reloadedCount++;
            }
        } catch (Exception e) {
            errors.add("Messages: " + e.getMessage());
            MessageUtils.send(sender, "<red>  ✘ <gray>Erro ao recarregar mensagens");
            MidgardLogger.error("Erro ao recarregar mensagens", e);
        }
        
        // 4. Reload de todos os Módulos
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, "<gradient:#a855f7:#ec4899>  ⟳ Recarregando módulos...</gradient>");
        
        if (MidgardCore.getModuleManager() != null) {
            Map<String, RPGModule> modules = MidgardCore.getModuleManager().getModules();
            
            for (Map.Entry<String, RPGModule> entry : modules.entrySet()) {
                RPGModule module = entry.getValue();
                if (module == null || !module.isEnabled()) continue;
                
                try {
                    module.reloadConfig();
                    MessageUtils.send(sender, "<green>    ✔ <gray>" + module.getName());
                    reloadedCount++;
                } catch (Exception e) {
                    errors.add(module.getName() + ": " + e.getMessage());
                    MessageUtils.send(sender, "<red>    ✘ <gray>" + module.getName() + " <dark_gray>- " + e.getMessage());
                    MidgardLogger.error("Erro ao recarregar módulo: " + module.getName(), e);
                }
            }
        }
        
        // 5. Resumo Final
        long elapsed = System.currentTimeMillis() - startTime;
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        if (errors.isEmpty()) {
            MessageUtils.send(sender, "<green>✔ Reload completo! <gray>" + reloadedCount + " componentes em <white>" + elapsed + "ms");
        } else {
            MessageUtils.send(sender, "<yellow>⚠ Reload concluído com " + errors.size() + " erro(s) em <white>" + elapsed + "ms");
            MessageUtils.send(sender, "<gray>Verifique o console para detalhes.");
        }
        MessageUtils.send(sender, "");
    }
    
    /**
     * Reload apenas da configuração principal
     */
    private void reloadConfig(CommandSender sender) {
        try {
            plugin.reloadConfig();
            MessageUtils.send(sender, "<green>✔ <gray>Configuração principal recarregada!");
        } catch (Exception e) {
            MessageUtils.send(sender, "<red>✘ <gray>Erro ao recarregar: " + e.getMessage());
            MidgardLogger.error("Erro ao recarregar config", e);
        }
    }
    
    /**
     * Reload apenas das mensagens
     */
    private void reloadMessages(CommandSender sender) {
        try {
            if (MidgardCore.getLanguageManager() != null) {
                String locale = plugin.getConfig().getString("settings.locale", "pt-br");
                MidgardCore.getLanguageManager().load(locale);
                MessageUtils.send(sender, "<green>✔ <gray>Mensagens recarregadas! <dark_gray>(locale: " + locale + ")");
            } else {
                MessageUtils.send(sender, "<red>✘ <gray>LanguageManager não disponível!");
            }
        } catch (Exception e) {
            MessageUtils.send(sender, "<red>✘ <gray>Erro ao recarregar mensagens: " + e.getMessage());
            MidgardLogger.error("Erro ao recarregar mensagens", e);
        }
    }
    
    /**
     * Reload apenas dos atributos
     */
    private void reloadAttributes(CommandSender sender) {
        try {
            int count = reloadAttributesInternal();
            MessageUtils.send(sender, "<green>✔ <gray>Atributos recarregados! <dark_gray>(" + count + " atributos)");
        } catch (Exception e) {
            MessageUtils.send(sender, "<red>✘ <gray>Erro ao recarregar atributos: " + e.getMessage());
            MidgardLogger.error("Erro ao recarregar atributos", e);
        }
    }
    
    /**
     * Reload interno dos atributos
     */
    private int reloadAttributesInternal() {
        // Limpa atributos existentes
        AttributeRegistry.getInstance().clear();
        
        // Recarrega do arquivo
        ConfigWrapper attributesConfig = new ConfigWrapper(plugin, "settings/attributes.yml");
        ConfigurationSection section = attributesConfig.getConfig().getConfigurationSection("attributes");
        
        int count = 0;
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection attrSection = section.getConfigurationSection(key);
                if (attrSection == null) continue;
                
                String name = attrSection.getString("name", key);
                String icon = attrSection.getString("icon", "");
                String format = attrSection.getString("format", "0.0");
                double base = attrSection.getDouble("base", 0.0);
                double min = attrSection.getDouble("min", 0.0);
                double max = attrSection.getDouble("max", 100000.0);
                
                Attribute attribute = new Attribute(key, name, base, min, max, icon, format);
                AttributeRegistry.getInstance().register(key, attribute);
                count++;
            }
        }
        
        MidgardLogger.info("Recarregados " + count + " atributos do arquivo.");
        return count;
    }
    
    /**
     * Reload de todos os módulos
     */
    private void reloadAllModules(CommandSender sender) {
        if (MidgardCore.getModuleManager() == null) {
            MessageUtils.send(sender, "<red>✘ <gray>ModuleManager não disponível!");
            return;
        }
        
        MessageUtils.send(sender, "<gradient:#a855f7:#ec4899>⟳ Recarregando todos os módulos...</gradient>");
        
        Map<String, RPGModule> modules = MidgardCore.getModuleManager().getModules();
        int success = 0;
        int failed = 0;
        
        for (Map.Entry<String, RPGModule> entry : modules.entrySet()) {
            RPGModule module = entry.getValue();
            if (module == null || !module.isEnabled()) continue;
            
            try {
                module.reloadConfig();
                MessageUtils.send(sender, "<green>  ✔ <gray>" + module.getName());
                success++;
            } catch (Exception e) {
                MessageUtils.send(sender, "<red>  ✘ <gray>" + module.getName());
                MidgardLogger.error("Erro ao recarregar módulo: " + module.getName(), e);
                failed++;
            }
        }
        
        MessageUtils.send(sender, "");
        if (failed == 0) {
            MessageUtils.send(sender, "<green>✔ <gray>Todos os " + success + " módulos recarregados!");
        } else {
            MessageUtils.send(sender, "<yellow>⚠ <gray>" + success + " OK, " + failed + " com erro(s).");
        }
    }
    
    /**
     * Reload de um módulo específico
     */
    private void reloadSpecificModule(CommandSender sender, String moduleName) {
        if (MidgardCore.getModuleManager() == null) {
            MessageUtils.send(sender, "<red>✘ <gray>ModuleManager não disponível!");
            return;
        }
        
        // Tenta encontrar o módulo (case-insensitive)
        RPGModule module = null;
        for (Map.Entry<String, RPGModule> entry : MidgardCore.getModuleManager().getModules().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(moduleName)) {
                module = entry.getValue();
                break;
            }
        }
        
        if (module == null) {
            MessageUtils.send(sender, "<red>✘ <gray>Módulo não encontrado: <white>" + moduleName);
            
            // Mostra módulos disponíveis
            String available = MidgardCore.getModuleManager().getModules().keySet()
                    .stream()
                    .collect(Collectors.joining("<dark_gray>, <gray>"));
            MessageUtils.send(sender, "<gray>Disponíveis: <gray>" + available);
            return;
        }
        
        if (!module.isEnabled()) {
            MessageUtils.send(sender, "<red>✘ <gray>Módulo está desabilitado: <white>" + module.getName());
            return;
        }
        
        try {
            long start = System.currentTimeMillis();
            module.reloadConfig();
            long elapsed = System.currentTimeMillis() - start;
            
            MessageUtils.send(sender, "<green>✔ <gray>Módulo <white>" + module.getName() + "<gray> recarregado! <dark_gray>(" + elapsed + "ms)");
        } catch (Exception e) {
            MessageUtils.send(sender, "<red>✘ <gray>Erro ao recarregar <white>" + module.getName() + "<gray>: " + e.getMessage());
            MidgardLogger.error("Erro ao recarregar módulo: " + module.getName(), e);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>(SUBCOMMANDS);
            
            // Adiciona nomes dos módulos
            if (MidgardCore.getModuleManager() != null) {
                suggestions.addAll(MidgardCore.getModuleManager().getModules().keySet());
            }
            
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("modules")) {
            if (MidgardCore.getModuleManager() != null) {
                return MidgardCore.getModuleManager().getModules().keySet().stream()
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        return Collections.emptyList();
    }
}
