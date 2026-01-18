package me.ray.midgard.core.script.actions;

import me.ray.midgard.core.script.Action;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ConsoleCommandAction implements Action {

    private final String command;

    public ConsoleCommandAction(String command) {
        this.command = command;
    }

    @Override
    public void execute(Player player) {
        String cmd = command.replace("%player%", player.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }
}
