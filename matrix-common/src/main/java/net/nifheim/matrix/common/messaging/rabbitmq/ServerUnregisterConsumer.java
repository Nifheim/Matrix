package net.nifheim.matrix.common.messaging.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import net.nifheim.matrix.api.messaging.StandardChannel;
import net.nifheim.matrix.common.messaging.message.ServerUnregisterMessage;
import org.slf4j.Logger;

public class ServerUnregisterConsumer extends AbstractRabbitMQConsumer<ServerUnregisterMessage> {

    public ServerUnregisterConsumer(RabbitMQService manager, Logger logger) {
        super(manager, logger, StandardChannel.SERVER_UNREGISTER.getChannelName(), BuiltinExchangeType.FANOUT, ServerUnregisterMessage.class);
    }

    @Override
    protected String getRoutingKey() {
        return "";
    }
}
