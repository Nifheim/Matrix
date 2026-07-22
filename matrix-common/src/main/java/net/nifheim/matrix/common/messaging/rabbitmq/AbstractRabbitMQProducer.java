package net.nifheim.matrix.common.messaging.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import net.nifheim.matrix.api.messaging.Message;
import net.nifheim.matrix.common.api.MatrixCommon;

public abstract class AbstractRabbitMQProducer <T extends Message> {

    protected final RabbitMQService manager;
    protected final String exchange;
    protected final BuiltinExchangeType exchangeType;

    public AbstractRabbitMQProducer(RabbitMQService manager, String exchange, BuiltinExchangeType exchangeType) {
        this.manager = manager;
        this.exchange = exchange;
        this.exchangeType = exchangeType;
    }

    public void sendMessage(T message) throws IOException, TimeoutException {
        try (Channel channel = manager.getConnection().createChannel()) {
            channel.exchangeDeclare(exchange, exchangeType);
            String json = MatrixCommon.GSON.toJson(message);
            channel.basicPublish(exchange, getRoutingKey(message), null, json.getBytes(StandardCharsets.UTF_8));
        }
    }

    protected abstract String getRoutingKey(T message);
}
