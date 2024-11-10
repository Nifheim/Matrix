package net.nifheim.matrix.velocity.listener.messaging;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import java.net.InetSocketAddress;
import net.nifheim.matrix.api.messaging.MessageListener;
import net.nifheim.matrix.api.messaging.message.StandardChannel;
import net.nifheim.matrix.api.server.ServerManager;
import net.nifheim.matrix.common.messaging.message.ServerRegisterMessage;
import org.slf4j.Logger;

public class ServerRegisterListener extends MessageListener<ServerRegisterMessage> {

    private final Logger logger;
    private final ProxyServer server;
    private final ServerManager serverManager;

    public ServerRegisterListener(Logger logger, ProxyServer server, ServerManager serverManager) {
        super(StandardChannel.SERVER_REGISTER);
        this.logger = logger;
        this.server = server;
        this.serverManager = serverManager;
    }

    @Override
    public void onMessage(ServerRegisterMessage message) {
        net.nifheim.matrix.api.server.ServerInfo serverInfo = message.getServerInfo();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(serverInfo.getAddress(), serverInfo.getPort());
        logger.info("Registering server {} ({}:{})", serverInfo.getName(), serverInfo.getAddress(), serverInfo.getPort());
        serverManager.addServer(serverInfo).thenRun(() -> {
            server.registerServer(new ServerInfo(serverInfo.getName(), inetSocketAddress));
            logger.info("Server {} registered", serverInfo.getName());
        });
    }
}
