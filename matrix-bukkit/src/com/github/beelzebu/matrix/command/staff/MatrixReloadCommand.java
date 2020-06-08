package com.github.beelzebu.matrix.command.staff;

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
        api.getConfig().reload();
        api.getMessagesMap().forEach((key, value) -> value.reload());
    }
}
