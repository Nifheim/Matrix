package com.github.beelzebu.matrix.bungee.messaging.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.MessageListener;
import com.github.beelzebu.matrix.api.messaging.message.Message;
import com.github.beelzebu.matrix.api.messaging.message.MessageType;
import com.github.beelzebu.matrix.messaging.message.ServerRegisterMessage;
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
public class ServerRegisterListener extends MessageListener {


    public ServerRegisterListener() {
        super(MessageType.SERVER_REGISTER);
    }

    @Override
    public void onMessage(Message message) {
        ServerInfoImpl info = (ServerInfoImpl) ServerRegisterMessage.getServerInfo(message);
        String address = ServerRegisterMessage.getAddress(message);
        int port = ServerRegisterMessage.getPort(message);
        if (ProxyServer.getInstance().getServers().containsKey(info.getServerName())) {
            Matrix.getLogger().info("Server already registered: " + info.getServerName());
            return;
        }
        Matrix.getLogger().info("Adding server: " + info.getServerName());
        ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(info.getServerName(), Util.getAddr(address + ":" + port), "", false);
        ProxyServer.getInstance().getServers().remove("lobby");
        Matrix.getAPI().getServerManager().addServer(info);
        for (ServerInfo storedServer : ProxyServer.getInstance().getServers().values()) {
            if (Objects.equals(storedServer.getSocketAddress(), Util.getAddr(address + ":" + port))) {
                Matrix.getLogger().info("Server with same address already registered: " + storedServer.getName() + ", new: " + info.getServerName());
                serverInfo = ProxyServer.getInstance().constructServerInfo(storedServer.getName(), Util.getAddr(address + ":" + port), "", false);
                break;
            }
        }
        ProxyServer.getInstance().getServers().put(info.getServerName(), serverInfo);
        if (info.getServerName().startsWith(ServerInfoImpl.AUTH_GROUP)) {
            Collection<ListenerInfo> listenerInfos = ProxyServer.getInstance().getConfig().getListeners();
            for (ListenerInfo listenerInfo : listenerInfos) {
                listenerInfo.getServerPriority().add(info.getServerName());
                listenerInfo.getServerPriority().removeIf(server -> !server.startsWith(ServerInfoImpl.AUTH_GROUP));
            }
        }
    }
}
