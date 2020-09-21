package com.github.beelzebu.matrix.bukkit.command.staff;

import cl.indiopikaro.bukkitutil.api.command.MatrixCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

/**
 * @author Beelzebu
 */
public class StopCommand extends MatrixCommand {

    public StopCommand() {
        super("stop", "matrix.admin", false, "minecraft:stop", "bukkit:stop", "spigot:stop", "restart", "spigot:restart");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            if (Bukkit.getPluginManager().getPlugin("MySQLPlayerDataBridge") != null) {
                Bukkit.dispatchCommand(sender, "mpdb saveandkick");
            }
            Bukkit.shutdown();
        }
    }
}
