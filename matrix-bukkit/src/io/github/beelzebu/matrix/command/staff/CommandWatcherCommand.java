package io.github.beelzebu.matrix.command.staff;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.commands.MatrixCommand;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.util.StringUtils;
import org.bukkit.command.CommandSender;

/**
 * @author Beelzebu
 */
public class CommandWatcherCommand extends MatrixCommand {

    public CommandWatcherCommand() {
        super("cw", "matrix.mod", "vc");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        MatrixPlayer mp = Matrix.getAPI().getPlayer(sender.getName());
        boolean status = !mp.isWatcher();
        mp.setWatcher(status);
        sender.sendMessage(StringUtils.replace("&7Command watcher: " + (status ? "&aactivado" : "&cdesactivado") + "&7."));
    }
}
