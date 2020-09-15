package com.github.beelzebu.matrix.bukkit.command.user;

import com.github.beelzebu.matrix.api.command.MatrixCommand;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.server.GameType;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.server.ServerInfoImpl;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.Objects;
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
            String lobbyServer = api.getServerInfo().getLobbyServer();
            if (Objects.equals(lobbyServer, api.getServerInfo().getServerName())) {
                out.writeUTF(ServerInfo.getLobbyServerName(ServerInfoImpl.MAIN_LOBBY_GROUP, GameType.NONE));
            } else {
                out.writeUTF(api.getServerInfo().getLobbyServer());
            }
            player.sendPluginMessage((Plugin) api.getPlugin().getBootstrap(), "BungeeCord", out.toByteArray());
        } else {
            sender.sendMessage(I18n.tl(Message.GENERAL_NO_CONSOLE, I18n.DEFAULT_LOCALE));
        }
    }
}
