package com.github.beelzebu.matrix.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.RedisMessageListener;
import com.github.beelzebu.matrix.api.messaging.message.RedisMessageType;
import com.github.beelzebu.matrix.api.messaging.message.ServerRegisterMessage;
import java.util.Collection;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;

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
        ProxyServer.getInstance().getServers().put(message.getName(), ProxyServer.getInstance().constructServerInfo(message.getName(), Util.getAddr(message.getIp() + ":" + message.getPort()), "", false));
        ProxyServer.getInstance().getServers().remove("lobby");
        Matrix.getAPI().getCache().registerGroup(message.getGroup());
        Matrix.getAPI().getCache().addServer(message.getGroup(), message.getName());
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
