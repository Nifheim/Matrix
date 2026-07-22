package net.nifheim.matrix.common.messaging.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import net.nifheim.matrix.common.messaging.message.LoginMessage;
import org.slf4j.Logger;

public class LoginConsumer extends AbstractRabbitMQConsumer<LoginMessage> {

    public LoginConsumer(RabbitMQService manager, Logger logger) {
        super(manager, logger, "login", BuiltinExchangeType.FANOUT, LoginMessage.class);
    }

    @Override
    protected String getRoutingKey() {
        return "";
    }
}
