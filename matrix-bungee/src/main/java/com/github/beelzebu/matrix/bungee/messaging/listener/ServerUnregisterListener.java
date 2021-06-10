package com.github.beelzebu.matrix.bungee.messaging.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.MessageListener;
import com.github.beelzebu.matrix.api.messaging.message.Message;
import com.github.beelzebu.matrix.api.messaging.message.MessageType;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.messaging.message.ServerUnregisterMessage;
import com.github.beelzebu.matrix.server.ServerInfoImpl;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;

/**
 * @author Beelzebu
 */
public class ServerUnregisterListener extends MessageListener {

    public ServerUnregisterListener() {
        super(MessageType.SERVER_UNREGISTER);
    }

    @Override
    public void onMessage(Message message) {
        ServerInfoImpl info = (ServerInfoImpl) ServerUnregisterMessage.getServerInfo(message);
        Matrix.getLogger().info("Received unregister message for server: " + info.getServerName());
        if (info.getServerType() != ServerType.AUTH) {
            ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(Matrix.getAPI().getServerManager().getLobbyForGroup(info.getGroupName()));
            if (serverInfo != null) {
                for (ProxiedPlayer proxiedPlayer : serverInfo.getPlayers()) {
                    proxiedPlayer.connect(serverInfo, ServerConnectEvent.Reason.SERVER_DOWN_REDIRECT);
                }
            }
        }
        ProxyServer.getInstance().getConfig().getListeners().forEach(listenerInfo -> listenerInfo.getServerPriority().removeIf(server -> server.equals(info.getServerName())));
        ProxyServer.getInstance().getServers().remove(info.getServerName());
        Matrix.getAPI().getServerManager().removeServer(info);
    }
}
