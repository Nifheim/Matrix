package com.github.beelzebu.matrix.bungee.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.RedisMessageListener;
import com.github.beelzebu.matrix.api.messaging.message.RedisMessageType;
import com.github.beelzebu.matrix.api.messaging.message.ServerUnregisterMessage;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.bungee.util.ServerUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;

/**
 * @author Beelzebu
 */
public class ServerUnregisterListener implements RedisMessageListener<ServerUnregisterMessage> {

    @Override
    public void onMessage(ServerUnregisterMessage message) {
        Matrix.getLogger().info("Received unregister message for server: " + message.getServerInfo().getServerName());
        if (message.getServerInfo().getServerType() != ServerType.AUTH) {
            ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(message.getServerInfo().getServerName());
            if (serverInfo != null) {
                for (ProxiedPlayer proxiedPlayer : serverInfo.getPlayers()) {
                    proxiedPlayer.connect(ServerUtil.getRandomLobby(serverInfo.getName()), ServerConnectEvent.Reason.SERVER_DOWN_REDIRECT);
                }
            }
        }
        ProxyServer.getInstance().getConfig().getListeners().forEach(listenerInfo -> listenerInfo.getServerPriority().removeIf(server -> server.equals(message.getServerInfo().getServerName())));
        ProxyServer.getInstance().getServers().remove(message.getServerInfo().getServerName());
        Matrix.getAPI().getCache().removeServer(message.getServerInfo());
    }

    @Override
    public RedisMessageType getType() {
        return RedisMessageType.SERVER_UNREGISTER;
    }
}
