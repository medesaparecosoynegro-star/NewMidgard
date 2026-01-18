package me.ray.midgard.proxy.command;

import com.velocitypowered.api.command.SimpleCommand;
import me.ray.midgard.proxy.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ReloadCommand implements SimpleCommand {

    private final ConfigManager configManager;

    public ReloadCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!invocation.source().hasPermission("midgard.admin")) {
             invocation.source().sendMessage(Component.text("Você não tem permissão para isso.", NamedTextColor.RED));
             return;
        }

        String[] args = invocation.arguments();
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            configManager.load();
            invocation.source().sendMessage(Component.text("Configuração do MidgardProxy recarregada!", NamedTextColor.GREEN));
        } else {
            invocation.source().sendMessage(Component.text("MidgardProxy v1.0.0. Use /midgardproxy reload", NamedTextColor.YELLOW));
        }
    }
}
