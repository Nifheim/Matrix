package com.github.beelzebu.matrix.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.RedisMessageListener;
import com.github.beelzebu.matrix.api.messaging.message.RedisMessageType;
import com.github.beelzebu.matrix.api.messaging.message.ServerRegisterMessage;
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
        if (ProxyServer.getInstance().getServers().containsKey(message.getName())) {
            Matrix.getLogger().info("Server already registered: " + message.getName());
            return;
        }
        Matrix.getLogger().info("Adding server: " + message.getName());
        ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(message.getName(), Util.getAddr(message.getIp() + ":" + message.getPort()), "", false);
        ProxyServer.getInstance().getServers().remove("lobby");
        Matrix.getAPI().getCache().registerGroup(message.getGroup());
        Matrix.getAPI().getCache().addServer(message.getGroup(), message.getName());
        for (ServerInfo storedServer : ProxyServer.getInstance().getServers().values()) {
            if (Objects.equals(storedServer.getSocketAddress(), Util.getAddr(message.getIp() + ":" + message.getPort()))) {
                serverInfo = ProxyServer.getInstance().constructServerInfo(storedServer.getName(), Util.getAddr(message.getIp() + ":" + message.getPort()), "", false);
                break;
            }
        }
        ProxyServer.getInstance().getServers().put(message.getName(), serverInfo);
        if (message.getName().startsWith("auth")) {
            Collection<ListenerInfo> listenerInfos = ProxyServer.getInstance().getConfig().getListeners();
            for (ListenerInfo listenerInfo : listenerInfos) {
                listenerInfo.getServerPriority().add(message.getName());
                listenerInfo.getServerPriority().removeIf(server -> !server.toLowerCase().contains("auth"));
            }
        }
    }

    @Override
    public RedisMessageType getType() {
        return RedisMessageType.SERVER_REGISTER;
    }
}
