package me.ray.midgard.loader.command;

import me.ray.midgard.core.MidgardCore;
import me.ray.midgard.core.command.MidgardCommand;
import me.ray.midgard.core.debug.MidgardLogger;
import me.ray.midgard.core.text.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Comando para escanear todo o código fonte e encontrar mensagens faltantes.
 * <p>
 * Uso: /midgard scanmessages [--generate]
 * <p>
 * Escaneia todos os módulos do projeto procurando por chamadas a:
 * - getMessage("chave")
 * - getRawMessage("chave")
 * - MessageKey.of("chave")
 * - MessageKey.builder("chave")
 * <p>
 * E verifica quais dessas chaves estão faltando nos arquivos YAML.
 *
 * @since 2.0.0
 */
public class ScanMessagesCommand extends MidgardCommand {

    private final JavaPlugin plugin;
    
    public ScanMessagesCommand(JavaPlugin plugin) {
        super("scanmessages", "midgard.admin.scan", false);
        this.plugin = plugin;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("scanmsg", "findmissing", "checkmessages");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        boolean autoGenerate = args.length > 0 && args[0].equalsIgnoreCase("--generate");
        
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, "<gradient:#a855f7:#ec4899>⚡ Escaneando mensagens faltantes...</gradient>");
        MessageUtils.send(sender, "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        MessageUtils.send(sender, "");
        
        // Encontrar o diretório raiz do projeto
        Path projectRoot = findProjectRoot();
        
        if (projectRoot == null) {
            MessageUtils.send(sender, "<red>✘ Não foi possível encontrar o diretório raiz do projeto.");
            MessageUtils.send(sender, "<gray>Certifique-se de que o plugin está sendo executado no ambiente de desenvolvimento.");
            return;
        }
        
        MessageUtils.send(sender, "<gray>Diretório do projeto: <white>" + projectRoot);
        MessageUtils.send(sender, "");
        
        try {
            // Executar o escaneamento
            MidgardCore.getLanguageManager().scanAndExposeAllMissingKeys(projectRoot);
            
            if (autoGenerate) {
                MessageUtils.send(sender, "");
                MessageUtils.send(sender, "<yellow>⚡ Gerando chaves faltantes automaticamente...");
                
                var report = MidgardCore.getLanguageManager().getValidator().scanAllModules(projectRoot);
                int generated = MidgardCore.getLanguageManager().getValidator().generateMissingKeys(report, true);
                
                if (generated > 0) {
                    MessageUtils.send(sender, "<green>✔ <white>Geradas <aqua>" + generated + " <white>chaves automaticamente!");
                    MessageUtils.send(sender, "<gray>Execute <yellow>/midgard reload messages <gray>para recarregar.");
                } else {
                    MessageUtils.send(sender, "<green>✔ <white>Nenhuma chave precisou ser gerada.");
                }
            }
            
            MessageUtils.send(sender, "");
            MessageUtils.send(sender, "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            MessageUtils.send(sender, "<green>✔ <white>Escaneamento concluído!");
            MessageUtils.send(sender, "");
            
        } catch (Exception e) {
            MessageUtils.send(sender, "<red>✘ Erro ao escanear: " + e.getMessage());
            MidgardLogger.error("Erro ao escanear mensagens", e);
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("--generate");
        }
        return Collections.emptyList();
    }
    
    /**
     * Encontra o diretório raiz do projeto.
     * Procura por build.gradle ou settings.gradle subindo na hierarquia.
     */
    private Path findProjectRoot() {
        try {
            // Começar do diretório do plugin
            Path current = plugin.getDataFolder().toPath().getParent();
            
            // Subir até encontrar build.gradle ou settings.gradle
            while (current != null) {
                if (current.resolve("build.gradle").toFile().exists() ||
                    current.resolve("settings.gradle").toFile().exists()) {
                    
                    // Verificar se tem a estrutura de módulos do Midgard
                    if (current.resolve("midgard-core").toFile().exists() ||
                        current.resolve("midgard-modules").toFile().exists()) {
                        return current;
                    }
                }
                current = current.getParent();
            }
            
            // Fallback: tentar caminhos conhecidos de desenvolvimento
            String[] devPaths = {
                    System.getProperty("user.home") + "/Desktop/MidgardRPG",
                    System.getProperty("user.dir"),
                    "C:/Users/ray/Desktop/MidgardRPG"
            };
            
            for (String devPath : devPaths) {
                Path path = Path.of(devPath);
                if (path.resolve("midgard-modules").toFile().exists()) {
                    return path;
                }
            }
            
        } catch (Exception e) {
            MidgardLogger.warn("Erro ao encontrar diretório raiz: %s", e.getMessage());
        }
        
        return null;
    }
}
