package com.github.beelzebu.matrix.command.staff;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.command.MatrixCommand;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
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
            matrixPlayer.setGameMode(com.github.beelzebu.matrix.api.player.GameMode.valueOf(player.getGameMode().toString()), Matrix.getAPI().getServerInfo().getGameType());
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
