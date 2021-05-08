package com.github.beelzebu.matrix.bungee.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.RedisMessageListener;
import com.github.beelzebu.matrix.api.messaging.message.RedisMessageType;
import com.github.beelzebu.matrix.api.messaging.message.ServerRegisterMessage;
import com.github.beelzebu.matrix.server.ServerInfoImpl;
import java.util.Collection;
import java.util.Objects;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * @author Beelzebu
 */
public class ServerRegisterListener implements RedisMessageListener<ServerRegisterMessage> {

    @Override
    public void onMessage(ServerRegisterMessage message) {
        if (ProxyServer.getInstance().getServers().containsKey(message.getServerInfo().getServerName())) {
            Matrix.getLogger().info("Server already registered: " + message.getServerInfo().getServerName());
            return;
        }
        Matrix.getLogger().info("Adding server: " + message.getServerInfo().getServerName());
        ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(message.getServerInfo().getServerName(), Util.getAddr(message.getIp() + ":" + message.getPort()), "", false);
        ProxyServer.getInstance().getServers().remove("lobby");
        Matrix.getAPI().getServerManager().addServer(message.getServerInfo());
        for (ServerInfo storedServer : ProxyServer.getInstance().getServers().values()) {
            if (Objects.equals(storedServer.getSocketAddress(), Util.getAddr(message.getIp() + ":" + message.getPort()))) {
                serverInfo = ProxyServer.getInstance().constructServerInfo(storedServer.getName(), Util.getAddr(message.getIp() + ":" + message.getPort()), "", false);
                break;
            }
        }
        ProxyServer.getInstance().getServers().put(message.getServerInfo().getServerName(), serverInfo);
        if (message.getServerInfo().getServerName().startsWith(ServerInfoImpl.AUTH_GROUP)) {
            Collection<ListenerInfo> listenerInfos = ProxyServer.getInstance().getConfig().getListeners();
            for (ListenerInfo listenerInfo : listenerInfos) {
                listenerInfo.getServerPriority().add(message.getServerInfo().getServerName());
                listenerInfo.getServerPriority().removeIf(server -> !server.startsWith(ServerInfoImpl.AUTH_GROUP));
            }
        }
    }

    @Override
    public RedisMessageType getType() {
        return RedisMessageType.SERVER_REGISTER;
    }
}
