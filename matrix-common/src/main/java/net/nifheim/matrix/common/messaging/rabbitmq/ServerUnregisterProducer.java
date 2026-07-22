package net.nifheim.matrix.common.messaging.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import net.nifheim.matrix.api.messaging.StandardChannel;
import net.nifheim.matrix.common.messaging.message.ServerUnregisterMessage;

public class ServerUnregisterProducer extends AbstractRabbitMQProducer<ServerUnregisterMessage> {

    public ServerUnregisterProducer(RabbitMQService manager) {
        super(manager, StandardChannel.SERVER_UNREGISTER.getChannelName(), BuiltinExchangeType.FANOUT);
    }

    @Override
    protected String getRoutingKey(ServerUnregisterMessage message) {
        return "";
    }
}
