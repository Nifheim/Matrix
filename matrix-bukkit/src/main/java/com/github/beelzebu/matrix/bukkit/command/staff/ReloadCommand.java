package com.github.beelzebu.matrix.bukkit.command.staff;

import cl.indiopikaro.bukkitutil.api.command.MatrixCommand;
import org.bukkit.command.CommandSender;

/**
 * @author Beelzebu
 */
public class ReloadCommand extends MatrixCommand {

    public ReloadCommand() {
        super("reload", "matrix.admin", "bukkit:reload", "spigot:reload", "rl", "bukkit:rl", "spigot:rl");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
    }
}
