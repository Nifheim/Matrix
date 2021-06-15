package com.github.beelzebu.matrix.bukkit.command.staff;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.command.MatrixCommand;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class CommandWatcherCommand extends MatrixCommand {

    public static @NotNull String PERMISSION = "matrix.command.commandwatcher";

    public CommandWatcherCommand() {
        super("cw", PERMISSION, "vc");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return;
        }
        Matrix.getAPI().getPlayerManager().getPlayer(((Player) sender).getUniqueId()).thenAccept(matrixPlayer -> {
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