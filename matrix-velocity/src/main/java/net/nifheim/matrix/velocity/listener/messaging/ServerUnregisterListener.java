package net.nifheim.matrix.velocity.listener.messaging;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.util.function.BiConsumer;
import net.nifheim.matrix.api.server.ServerInfo;
import net.nifheim.matrix.api.server.ServerManager;
import net.nifheim.matrix.common.messaging.message.ServerUnregisterMessage;
import net.nifheim.matrix.common.messaging.rabbitmq.AbstractRabbitMQConsumer;
import org.slf4j.Logger;

public class ServerUnregisterListener implements BiConsumer<ServerUnregisterMessage, AbstractRabbitMQConsumer.DeliveryContext> {

    private final Logger logger;
    private final ProxyServer server;
    private final ServerManager serverManager;

    public ServerUnregisterListener(Logger logger, ProxyServer server, ServerManager serverManager) {
        this.logger = logger;
        this.server = server;
        this.serverManager = serverManager;
    }

    @Override
    public void accept(ServerUnregisterMessage message, AbstractRabbitMQConsumer.DeliveryContext context) {
        ServerInfo serverInfo = ServerUnregisterMessage.getServerInfo(message);
        logger.info("Unregistering server {} ({}:{})", serverInfo.getName(), serverInfo.getAddress(), serverInfo.getPort());
        server.getServer(serverInfo.getName()).map(RegisteredServer::getServerInfo).ifPresent(server::unregisterServer);
        serverManager.removeServer(serverInfo);
        logger.info("Server {} unregistered", serverInfo.getName());
        try {
            context.acknowledge();
        } catch (Exception e) {
            logger.error("Error acknowledging server unregister message", e);
        }
    }
}
