package com.github.beelzebu.matrix.command.user;

import com.github.beelzebu.matrix.api.command.MatrixCommand;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * @author Beelzebu
 */
public class LobbyCommand extends MatrixCommand {

    public LobbyCommand() {
        super("lobby", null, true);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(api.getServerInfo().getLobbyServer());
            player.sendPluginMessage((Plugin) api.getPlugin().getBootstrap(), "BungeeCord", out.toByteArray());
        } else {
            sender.sendMessage(I18n.tl(Message.GENERAL_NO_CONSOLE, I18n.DEFAULT_LOCALE));
        }
    }
}
