package me.ray.midgard.modules.essentials.command;

import me.ray.midgard.core.command.MidgardCommand;
import me.ray.midgard.modules.essentials.manager.EssentialsManager;

public abstract class EssentialsBaseCommand extends MidgardCommand {

    protected final EssentialsManager manager;

    public EssentialsBaseCommand(EssentialsManager manager, String name, String permission, boolean playerOnly) {
        super(name, permission, playerOnly);
        this.manager = manager;
    }
}
