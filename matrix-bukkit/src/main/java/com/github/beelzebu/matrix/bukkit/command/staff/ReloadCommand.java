package com.github.beelzebu.matrix.bukkit.command.staff;

import com.github.beelzebu.matrix.bukkit.command.MatrixCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class ReloadCommand extends MatrixCommand {

    public ReloadCommand() {
        super("reload", "matrix.admin", false, "bukkit:reload", "spigot:reload", "rl", "bukkit:rl", "spigot:rl");
    }

    @Override
    public void onCommand(CommandSender sender, String label, String @NotNull [] args) {
    }
}
