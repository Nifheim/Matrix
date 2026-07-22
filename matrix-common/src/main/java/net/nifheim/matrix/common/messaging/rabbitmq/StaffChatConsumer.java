package net.nifheim.matrix.common.messaging.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import net.nifheim.matrix.api.messaging.StandardChannel;
import net.nifheim.matrix.common.messaging.message.StaffChatMessage;
import org.slf4j.Logger;

public class StaffChatConsumer extends AbstractRabbitMQConsumer<StaffChatMessage> {

    public StaffChatConsumer(RabbitMQService manager, Logger logger) {
        super(manager, logger, StandardChannel.MESSAGE_BROADCAST.getChannelName(), BuiltinExchangeType.FANOUT, StaffChatMessage.class);
    }

    @Override
    protected String getRoutingKey() {
        return "";
    }
}
