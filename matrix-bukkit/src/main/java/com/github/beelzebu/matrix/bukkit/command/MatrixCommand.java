package com.github.beelzebu.matrix.bukkit.command;

import com.github.beelzebu.matrix.api.Matrix;
import net.nifheim.bukkit.commandlib.RegistrableCommand;
import org.bukkit.plugin.Plugin;

/**
 * @author Jaime Suárez
 */
public abstract class MatrixCommand extends RegistrableCommand {

    public MatrixCommand(String command, String permission, boolean async, String... aliases) {
        super((Plugin) Matrix.getAPI().getPlugin().getBootstrap(), command, permission, async, aliases);
    }
}
