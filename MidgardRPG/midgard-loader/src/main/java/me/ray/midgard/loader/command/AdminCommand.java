package me.ray.midgard.loader.command;

import me.ray.midgard.core.command.AdminCommandRegistry;
import me.ray.midgard.core.command.MidgardCommand;
import me.ray.midgard.core.text.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import java.util.*;

/**
 * Comando central de administração do MidgardRPG.
 * Agrupa todos os subcomandos administrativos sob /rpg admin <subcomando>
 * 
 * Uso: /rpg admin <reload|reset|stats|scan|item|performance>
 */
public class AdminCommand extends MidgardCommand implements AdminCommandRegistry {

    private final Map<String, MidgardCommand> subcommands = new LinkedHashMap<>();
    private final JavaPlugin plugin;

    public AdminCommand(JavaPlugin plugin) {
        super("admin", "midgard.admin", false);
        this.plugin = plugin;
        
        // Registrar subcomandos de admin
        registerSubcommand(new ReloadCommand(plugin));
        registerSubcommand(new ResetCommand());
        registerSubcommand(new StatsCommand());
        registerSubcommand(new ScanMessagesCommand(plugin));
    }

    /**
     * Registra um subcomando de admin.
     * 
     * @param command O comando a ser registrado
     */
    @Override
    public void registerSubcommand(MidgardCommand command) {
        if (command == null) {
            return;
        }
        subcommands.put(command.getName().toLowerCase(), command);
        for (String alias : command.getAliases()) {
            subcommands.put(alias.toLowerCase(), command);
        }
    }

    /**
     * Remove um subcomando de admin.
     * 
     * @param name Nome do subcomando
     */
    @Override
    public void unregisterSubcommand(String name) {
        if (name == null) {
            return;
        }
        subcommands.remove(name.toLowerCase());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender == null) {
            return;
        }
        
        if (args.length == 0) {
            sendAdminHelp(sender);
            return;
        }

        String subcommandName = args[0].toLowerCase();
        MidgardCommand subcommand = subcommands.get(subcommandName);

        if (subcommand == null) {
            MessageUtils.send(sender, "<red>✘ Subcomando desconhecido: <white>" + subcommandName);
            MessageUtils.send(sender, "<gray>Use <yellow>/rpg admin help <gray>para ver os comandos disponíveis.");
            return;
        }

        // Verificar permissão específica do subcomando
        String subPerm = subcommand.getPermission();
        if (subPerm != null && !sender.hasPermission(subPerm)) {
            MessageUtils.send(sender, "<red>✘ Você não tem permissão para usar este comando.");
            return;
        }

        // Executar subcomando com argumentos restantes
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        subcommand.execute(sender, subArgs);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (sender == null) {
            return Collections.emptyList();
        }
        
        if (args.length == 1) {
            // Sugerir subcomandos de admin
            List<String> completions = new ArrayList<>();
            for (Map.Entry<String, MidgardCommand> entry : subcommands.entrySet()) {
                MidgardCommand cmd = entry.getValue();
                // Só sugerir se o nome for igual à chave (evita duplicatas de aliases)
                if (entry.getKey().equals(cmd.getName().toLowerCase())) {
                    String perm = cmd.getPermission();
                    if (perm == null || sender.hasPermission(perm)) {
                        completions.add(cmd.getName());
                    }
                }
            }
            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());
        } else if (args.length > 1) {
            // Delegar tab completion ao subcomando
            String subcommandName = args[0].toLowerCase();
            MidgardCommand subcommand = subcommands.get(subcommandName);
            if (subcommand != null) {
                String perm = subcommand.getPermission();
                if (perm == null || sender.hasPermission(perm)) {
                    String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                    return subcommand.tabComplete(sender, subArgs);
                }
            }
        }
        return Collections.emptyList();
    }

    private void sendAdminHelp(CommandSender sender) {
        MessageUtils.send(sender, "");
        MessageUtils.send(sender, "<gradient:#a855f7:#ec4899><bold>⚔ MidgardRPG Admin Commands</bold></gradient>");
        MessageUtils.send(sender, "<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        Set<String> shown = new HashSet<>();
        for (Map.Entry<String, MidgardCommand> entry : subcommands.entrySet()) {
            MidgardCommand cmd = entry.getValue();
            // Evitar duplicatas (aliases)
            if (shown.contains(cmd.getName())) {
                continue;
            }
            shown.add(cmd.getName());
            
            String perm = cmd.getPermission();
            if (perm == null || sender.hasPermission(perm)) {
                String description = getCommandDescription(cmd.getName());
                MessageUtils.send(sender, "<gray>• <yellow>/rpg admin " + cmd.getName() + " <dark_gray>- <gray>" + description);
            }
        }
        
        MessageUtils.send(sender, "");
    }

    private String getCommandDescription(String commandName) {
        return switch (commandName.toLowerCase()) {
            case "reload" -> "Recarrega configurações e módulos";
            case "reset" -> "Reseta dados de um jogador";
            case "stats" -> "Visualiza atributos de um jogador";
            case "scan", "scanmessages" -> "Escaneia chaves de mensagens";
            case "item" -> "Gerencia itens do sistema";
            case "performance" -> "Métricas de performance";
            case "class" -> "Gerencia classes";
            case "spell" -> "Gerencia magias";
            default -> "Comando administrativo";
        };
    }

    @Override
    public List<String> getAliases() {
        return List.of("adm", "a");
    }
}
