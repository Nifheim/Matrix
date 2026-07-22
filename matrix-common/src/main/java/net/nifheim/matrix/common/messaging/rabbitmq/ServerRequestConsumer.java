package net.nifheim.matrix.common.messaging.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import net.nifheim.matrix.api.messaging.StandardChannel;
import net.nifheim.matrix.common.messaging.message.ServerRequestMessage;
import org.slf4j.Logger;

public class ServerRequestConsumer extends AbstractRabbitMQConsumer<ServerRequestMessage> {

    public ServerRequestConsumer(RabbitMQService manager, Logger logger) {
        super(manager, logger, StandardChannel.SERVER_REQUEST.getChannelName(), BuiltinExchangeType.FANOUT, ServerRequestMessage.class);
    }

    @Override
    protected String getRoutingKey() {
        return "";
    }
}
