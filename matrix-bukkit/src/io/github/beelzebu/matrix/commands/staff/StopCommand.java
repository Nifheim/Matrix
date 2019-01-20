package io.github.beelzebu.matrix.commands.staff;

import io.github.beelzebu.matrix.api.commands.MatrixCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * @author Beelzebu
 */
public class StopCommand extends MatrixCommand {

    public StopCommand() {
        super("stop", "matrix.admin", "minecraft:stop", "bukkit:stop", "spigot:stop", "restart", "spigot:restart");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            Bukkit.shutdown();
        }
    }
}
