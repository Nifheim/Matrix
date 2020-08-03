package com.github.beelzebu.matrix.command.staff;

import cl.indiopikaro.jmatrix.api.Matrix;
import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.command.MatrixCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
public class StaffModeCommand extends MatrixCommand {

    public StaffModeCommand() {
        super("staffmode", "matrix.command.staffmode", false, "vanish", "ev");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(player.getUniqueId());
            boolean vanished = matrixPlayer.isVanished();
        }
    }
}
