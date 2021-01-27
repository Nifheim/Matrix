package com.github.beelzebu.matrix.bukkit.command.staff;

import cl.indiopikaro.bukkitutil.api.command.MatrixCommand;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
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
            api.getPlayerManager().getPlayer(player.getUniqueId()).thenAccept(matrixPlayer -> {
                boolean vanished = !matrixPlayer.isVanished();
                matrixPlayer.setVanished(vanished);
                matrixPlayer.setGameMode(com.github.beelzebu.matrix.api.player.GameMode.valueOf(player.getGameMode().toString()), Matrix.getAPI().getServerInfo().getGroupName());
                if (vanished) {
                    matrixPlayer.sendMessage(I18n.tl(Message.ESSENTIALS_VANISH_ENABLED, matrixPlayer.getLastLocale()));
                } else {
                    matrixPlayer.sendMessage(I18n.tl(Message.ESSENTIALS_VANISH_DISABLED, matrixPlayer.getLastLocale()));
                }
                if (vanished) {
                    player.setGameMode(GameMode.SPECTATOR);
                } else {
                    player.setGameMode(GameMode.valueOf(matrixPlayer.getGameMode(api.getServerInfo().getGroupName()).toString()));
                }
            });
        }
    }
}
