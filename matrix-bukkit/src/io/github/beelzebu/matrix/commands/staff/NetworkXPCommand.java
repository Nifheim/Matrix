package io.github.beelzebu.matrix.commands.staff;

import io.github.beelzebu.matrix.api.commands.MatrixCommand;
import org.bukkit.command.CommandSender;

/**
 * @author Beelzebu
 */
public class NetworkXPCommand extends MatrixCommand {

    public NetworkXPCommand(String command, String permission, String... aliases) {
        super("nifheimxp", "matrix.staff.admin", "nexp", "nxp");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        throw new UnsupportedOperationException("onCommand is not finished yet.");
    }
}
