package net.nifheim.matrix.velocity.listener.messaging;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.nifheim.matrix.api.messaging.MessageListener;
import net.nifheim.matrix.api.messaging.message.StandardChannel;
import net.nifheim.matrix.api.server.ServerInfo;
import net.nifheim.matrix.api.server.ServerManager;
import net.nifheim.matrix.common.messaging.message.ServerUnregisterMessage;
import org.slf4j.Logger;

public class ServerUnregisterListener extends MessageListener<ServerUnregisterMessage> {

    private final Logger logger;
    private final ProxyServer server;
    private final ServerManager serverManager;

    public ServerUnregisterListener(Logger logger, ProxyServer server, ServerManager serverManager) {
        super(StandardChannel.SERVER_UNREGISTER);
        this.logger = logger;
        this.server = server;
        this.serverManager = serverManager;
    }

    @Override
    public void onMessage(ServerUnregisterMessage message) {
        ServerInfo serverInfo = ServerUnregisterMessage.getServerInfo(message);
        logger.info("Unregistering server {} ({}:{})", serverInfo.getName(), serverInfo.getAddress(), serverInfo.getPort());
        server.getServer(serverInfo.getName()).map(RegisteredServer::getServerInfo).ifPresent(server::unregisterServer);
        serverManager.removeServer(serverInfo);
        logger.info("Server {} unregistered", serverInfo.getName());

    }
}
