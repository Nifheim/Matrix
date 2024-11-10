package net.nifheim.matrix.bungee.messaging.listener;

import net.nifheim.matrix.api.Matrix;
import net.nifheim.matrix.api.messaging.MessageListener;
import net.nifheim.matrix.api.messaging.message.Message;
import net.nifheim.matrix.api.messaging.message.MessageType;
import net.nifheim.matrix.common.messaging.message.ServerRegisterMessage;
import net.nifheim.matrix.common.server.ServerInfoImpl;
import java.util.Collection;
import java.util.Objects;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * @author Jaime Suárez
 */
public class ServerRegisterListener extends MessageListener {

    public ServerRegisterListener() {
        super(MessageType.SERVER_REGISTER);
    }

    @Override
    public void onMessage(Message message) {
        ServerInfoImpl info = (ServerInfoImpl) ServerRegisterMessage.getServerInfo(message);
        String address = ServerRegisterMessage.getAddress(message);
        if (Objects.equals(address, "0.0.0.0")) { // pterodactyl docker network workaround
            address = "172.1.0.1";
        }
        int port = ServerRegisterMessage.getPort(message);
        if (ProxyServer.getInstance().getServers().containsKey(info.getServerName())) {
            Matrix.getLogger().info("Server already registered: " + info.getServerName());
            return;
        }
        Matrix.getLogger().info("Adding server: " + info.getServerName() + " " + address + ":" + port);
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
