package net.nifheim.matrix.paper.command.staff;

import net.nifheim.matrix.paper.command.MatrixCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jaime Suárez
 */
public class ReloadCommand extends MatrixCommand {

    public ReloadCommand(Plugin plugin) {
        super(plugin, "reload", "matrix.admin", false, "bukkit:reload", "spigot:reload", "rl", "bukkit:rl", "spigot:rl");
    }

    @Override
    public void onCommand(CommandSender sender, String label, String @NotNull [] args) {
    }
}
