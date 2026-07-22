package net.nifheim.matrix.common.messaging.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import net.nifheim.matrix.api.messaging.StandardChannel;
import net.nifheim.matrix.common.messaging.message.StaffChatMessage;

public class StaffChatProducer extends AbstractRabbitMQProducer<StaffChatMessage> {

    public StaffChatProducer(RabbitMQService manager) {
        super(manager, StandardChannel.MESSAGE_BROADCAST.getChannelName(), BuiltinExchangeType.FANOUT);
    }

    @Override
    protected String getRoutingKey(StaffChatMessage message) {
        return "";
    }
}
