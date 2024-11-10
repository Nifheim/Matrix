package net.nifheim.matrix.paper.command;

import net.nifheim.bukkit.commandlib.RegistrableCommand;
import org.bukkit.plugin.Plugin;

/**
 * @author Jaime Suárez
 */
public abstract class MatrixCommand extends RegistrableCommand {

    public MatrixCommand(Plugin plugin, String command, String permission, boolean async, String... aliases) {
        super(plugin, command, permission, async, aliases);
    }
}
