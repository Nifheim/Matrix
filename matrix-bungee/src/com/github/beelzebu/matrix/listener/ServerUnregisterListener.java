package com.github.beelzebu.matrix.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.RedisMessageListener;
import com.github.beelzebu.matrix.api.messaging.message.RedisMessageType;
import com.github.beelzebu.matrix.api.messaging.message.ServerUnregisterMessage;
import com.github.beelzebu.matrix.util.ServerUtil;
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
        Matrix.getLogger().info("Received unregister message for server: " + message.getName());
        if (!message.getName().startsWith("auth")) {
            ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(message.getName());
            if (serverInfo != null) {
                for (ProxiedPlayer proxiedPlayer : serverInfo.getPlayers()) {
                    proxiedPlayer.connect(ServerUtil.getRandomLobby(serverInfo.getName()), ServerConnectEvent.Reason.SERVER_DOWN_REDIRECT);
                }
            }
        }
        ProxyServer.getInstance().getConfig().getListeners().forEach(listenerInfo -> listenerInfo.getServerPriority().removeIf(server -> server.equals(message.getName())));
        ProxyServer.getInstance().getServers().remove(message.getName());
        Matrix.getAPI().getCache().removeServer(message.getName());
        Matrix.getAPI().getCache().getAllServers().values().forEach(group -> group.forEach(server -> {
            if (ProxyServer.getInstance().getServerInfo(server) == null) {
                Matrix.getAPI().getCache().removeServer(server);
            }
        }));
    }

    @Override
    public RedisMessageType getType() {
        return RedisMessageType.SERVER_UNREGISTER;
    }
}
