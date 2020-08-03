package com.github.beelzebu.matrix.command.staff;

import cl.indiopikaro.jmatrix.api.Matrix;
import cl.indiopikaro.jmatrix.api.i18n.I18n;
import cl.indiopikaro.jmatrix.api.i18n.Message;
import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.command.MatrixCommand;
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
            matrixPlayer.setGameMode(cl.indiopikaro.jmatrix.api.player.GameMode.valueOf(player.getGameMode().toString()), Matrix.getAPI().getServerInfo().getGameType());
            if (vanished) {
                matrixPlayer.sendMessage(I18n.tl(Message.ESSENTIALS_VANISH_ENABLED, matrixPlayer.getLastLocale()));
            } else {
                matrixPlayer.sendMessage(I18n.tl(Message.ESSENTIALS_VANISH_DISABLED, matrixPlayer.getLastLocale()));
            }
            if (vanished) {
                player.setGameMode(GameMode.SPECTATOR);
            } else {
                player.setGameMode(GameMode.valueOf(matrixPlayer.getGameMode(api.getServerInfo().getGameType()).toString()));
            }
        }
    }
}
