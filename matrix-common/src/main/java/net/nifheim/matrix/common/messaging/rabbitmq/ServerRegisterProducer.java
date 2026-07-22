package net.nifheim.matrix.common.messaging.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import net.nifheim.matrix.api.messaging.StandardChannel;
import net.nifheim.matrix.common.messaging.message.ServerRegisterMessage;

public class ServerRegisterProducer extends AbstractRabbitMQProducer<ServerRegisterMessage> {

    public ServerRegisterProducer(RabbitMQService manager) {
        super(manager, StandardChannel.SERVER_REGISTER.getChannelName(), BuiltinExchangeType.FANOUT);
    }

    @Override
    protected String getRoutingKey(ServerRegisterMessage message) {
        return "";
    }
}
