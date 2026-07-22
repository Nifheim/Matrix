package net.nifheim.matrix.velocity.listener.messaging;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import java.net.InetSocketAddress;
import java.util.function.BiConsumer;
import net.nifheim.matrix.api.server.ServerManager;
import net.nifheim.matrix.common.messaging.message.ServerRegisterMessage;
import net.nifheim.matrix.common.messaging.rabbitmq.AbstractRabbitMQConsumer;
import org.slf4j.Logger;

public class ServerRegisterListener implements BiConsumer<ServerRegisterMessage, AbstractRabbitMQConsumer.DeliveryContext> {

    private final Logger logger;
    private final ProxyServer server;
    private final ServerManager serverManager;

    public ServerRegisterListener(Logger logger, ProxyServer server, ServerManager serverManager) {
        this.logger = logger;
        this.server = server;
        this.serverManager = serverManager;
    }

    @Override
    public void accept(ServerRegisterMessage message, AbstractRabbitMQConsumer.DeliveryContext context) {
        net.nifheim.matrix.api.server.ServerInfo serverInfo = message.getServerInfo();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(serverInfo.getAddress(), serverInfo.getPort());
        logger.info("Registering server {} ({}:{})", serverInfo.getName(), serverInfo.getAddress(), serverInfo.getPort());
        serverManager.addServer(serverInfo).thenRun(() -> {
            server.registerServer(new ServerInfo(serverInfo.getName(), inetSocketAddress));
            logger.info("Server {} registered", serverInfo.getName());
            try {
                context.acknowledge();
            } catch (Exception e) {
                logger.error("Error acknowledging server register message", e);
            }
        });
    }
}
