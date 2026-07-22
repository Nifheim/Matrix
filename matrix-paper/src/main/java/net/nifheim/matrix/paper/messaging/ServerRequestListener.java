package net.nifheim.matrix.paper.messaging;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import net.nifheim.matrix.common.messaging.MessagingService;
import net.nifheim.matrix.common.messaging.message.ServerRequestMessage;
import net.nifheim.matrix.common.messaging.rabbitmq.AbstractRabbitMQConsumer;
import net.nifheim.matrix.common.messaging.rabbitmq.ServerRegisterProducer;
import net.nifheim.matrix.paper.MatrixPaper;

/**
 * @author Jaime Suárez
 */
public class ServerRequestListener implements BiConsumer<ServerRequestMessage, AbstractRabbitMQConsumer.DeliveryContext> {

    private final MatrixPaper plugin;

    public ServerRequestListener(MatrixPaper plugin) {
        this.plugin = plugin;
    }

    @Override
    public void accept(ServerRequestMessage message, AbstractRabbitMQConsumer.DeliveryContext context) {
        plugin.getPlatformLogger().info("Proxy is requesting all servers, sending info...");
        if (plugin.getServerRegisterMessage() != null) {
            plugin.getPlatformLogger().info("Sending already created server register message.");
            try {
                MessagingService messagingService = plugin.getPlugin().getApi().getMessaging();
                messagingService.getProducer(ServerRegisterProducer.class).sendMessage(plugin.getServerRegisterMessage());
            } catch (IOException | TimeoutException e) {
                plugin.getPlatformLogger().error("Error sending server register message", e);
            }
        } else {
            plugin.getPlatformLogger().info("Server register message is null in this server.");
        }
        try {
            context.acknowledge();
        } catch (Exception e) {
            plugin.getPlatformLogger().error("Error acknowledging server request message", e);
        }
    }
}
