package com.github.beelzebu.matrix.bukkit.command.staff;

import cl.indiopikaro.bukkitutil.api.command.MatrixCommand;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
public class CommandWatcherCommand extends MatrixCommand {

    public static String PERMISSION = "matrix.command.commandwatcher";

    public CommandWatcherCommand() {
        super("cw", PERMISSION, "vc");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return;
        }
        api.getPlayerManager().getPlayer((Player) sender).thenAccept(matrixPlayer -> {
            boolean status = !matrixPlayer.isWatcher();
            matrixPlayer.setWatcher(status);
            if (status) {
                sender.sendMessage(I18n.tl(Message.CHAT_COMMAND_WATCHER_ENABLED, matrixPlayer.getLastLocale()));
            } else {
                sender.sendMessage(I18n.tl(Message.CHAT_COMMAND_WATCHER_DISABLED, matrixPlayer.getLastLocale()));
            }
        });
    }
}