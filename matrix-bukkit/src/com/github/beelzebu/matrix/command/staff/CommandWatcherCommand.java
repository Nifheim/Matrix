package com.github.beelzebu.matrix.command.staff;

import cl.indiopikaro.jmatrix.api.Matrix;
import cl.indiopikaro.jmatrix.api.i18n.I18n;
import cl.indiopikaro.jmatrix.api.i18n.Message;
import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.command.MatrixCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
public class CommandWatcherCommand extends MatrixCommand {

    public CommandWatcherCommand() {
        super("cw", "matrix.command.commandwatcher", "vc");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return;
        }
        MatrixPlayer matrixPlayer = Matrix.getAPI().getPlayer(((Player) sender).getUniqueId());
        boolean status = !matrixPlayer.isWatcher();
        matrixPlayer.setWatcher(status);
        if (status) {
            sender.sendMessage(I18n.tl(Message.CHAT_COMMAND_WATCHER_ENABLED, matrixPlayer.getLastLocale()));
        } else {
            sender.sendMessage(I18n.tl(Message.CHAT_COMMAND_WATCHER_DISABLED, matrixPlayer.getLastLocale()));
        }
    }
}