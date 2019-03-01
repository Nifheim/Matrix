package io.github.beelzebu.matrix.command.staff;

import io.github.beelzebu.matrix.api.commands.MatrixCommand;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
public class VanishCommand extends MatrixCommand {

    public static final String PERMISSION = "matrix.command.vanish";

    public VanishCommand() {
        super("vanish", PERMISSION, false, "evanish", "ev");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            MatrixPlayer matrixPlayer = api.getPlayer(player.getUniqueId());
            boolean vanished = !matrixPlayer.isVanished();
            matrixPlayer.setVanished(vanished);
            matrixPlayer.sendMessage("&7Tu vanish ha sido: " + (vanished ? "&aactivado" : "&cdesactivado"));
            if (vanished) {
                player.setGameMode(GameMode.SPECTATOR);
            } else {
                player.setGameMode(GameMode.SURVIVAL);
            }
        }
    }
}
