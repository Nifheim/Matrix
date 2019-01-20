package io.github.beelzebu.matrix.commands.staff;

import io.github.beelzebu.matrix.api.commands.MatrixCommand;
import org.bukkit.command.CommandSender;

/**
 * @author Beelzebu
 */
public class ReloadCommand extends MatrixCommand {

    public ReloadCommand() {
        super("reload", "matrix.admin");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
    }
}
