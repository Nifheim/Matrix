package com.github.beelzebu.matrix.bukkit.command.staff;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.command.MatrixCommand;
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
        Matrix.getAPI().reload();
    }
}
