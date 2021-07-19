package com.github.beelzebu.matrix.bukkit.command.staff;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.bukkit.command.MatrixCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class MatrixReloadCommand extends MatrixCommand {

    public MatrixReloadCommand() {
        super("matrixreload", "matrix.staff.admin", true);
    }

    @Override
    public void onCommand(CommandSender sender, String label, String @NotNull [] args) {
        Matrix.getAPI().reload();
    }
}
