package net.nifheim.matrix.common.messaging.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import net.nifheim.matrix.common.messaging.message.LoginMessage;

public class LoginProducer extends AbstractRabbitMQProducer<LoginMessage> {

    public LoginProducer(RabbitMQService manager) {
        super(manager, "login", BuiltinExchangeType.FANOUT);
    }

    @Override
    protected String getRoutingKey(LoginMessage message) {
        return "";
    }
}
