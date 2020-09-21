package com.github.beelzebu.matrix.bukkit.command.staff;

import cl.indiopikaro.bukkitutil.api.command.MatrixCommand;
import org.bukkit.command.CommandSender;

/**
 * @author Beelzebu
 */
public class MatrixReloadCommand extends MatrixCommand {

    public MatrixReloadCommand() {
        super("matrixreload", "matrix.staff.admin");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        api.reload();
    }
}
