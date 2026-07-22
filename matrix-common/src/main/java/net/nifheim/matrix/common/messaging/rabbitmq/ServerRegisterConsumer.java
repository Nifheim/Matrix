package net.nifheim.matrix.common.messaging.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import net.nifheim.matrix.api.messaging.StandardChannel;
import net.nifheim.matrix.common.messaging.message.ServerRegisterMessage;
import org.slf4j.Logger;

public class ServerRegisterConsumer extends AbstractRabbitMQConsumer<ServerRegisterMessage> {

    public ServerRegisterConsumer(RabbitMQService manager, Logger logger) {
        super(manager, logger, StandardChannel.SERVER_REGISTER.getChannelName(), BuiltinExchangeType.FANOUT, ServerRegisterMessage.class);
    }

    @Override
    protected String getRoutingKey() {
        return "";
    }
}
