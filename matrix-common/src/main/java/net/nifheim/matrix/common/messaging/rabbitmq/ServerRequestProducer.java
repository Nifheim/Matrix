package net.nifheim.matrix.common.messaging.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import net.nifheim.matrix.api.messaging.StandardChannel;
import net.nifheim.matrix.common.messaging.message.ServerRequestMessage;

public class ServerRequestProducer extends AbstractRabbitMQProducer<ServerRequestMessage> {

    public ServerRequestProducer(RabbitMQService manager) {
        super(manager, StandardChannel.SERVER_REQUEST.getChannelName(), BuiltinExchangeType.FANOUT);
    }

    @Override
    protected String getRoutingKey(ServerRequestMessage message) {
        return "";
    }
}
