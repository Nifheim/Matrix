package net.nifheim.matrix.common.messaging.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import net.nifheim.matrix.api.messaging.Message;
import net.nifheim.matrix.common.api.MatrixCommon;
import org.slf4j.Logger;

public abstract class AbstractRabbitMQConsumer <T extends Message> {

    protected final RabbitMQService manager;
    protected final Logger logger;
    protected final String exchange;
    protected final BuiltinExchangeType exchangeType;
    protected final Class<T> messageClass;
    private Channel channel;

    public AbstractRabbitMQConsumer(RabbitMQService manager, Logger logger, String exchange, BuiltinExchangeType exchangeType, Class<T> messageClass) {
        this.manager = manager;
        this.logger = logger;
        this.exchange = exchange;
        this.exchangeType = exchangeType;
        this.messageClass = messageClass;
    }

    public void consume(BiConsumer<T, DeliveryContext> consumer) throws IOException {
        consume(null, consumer);
    }

    public void consume(String queueName, BiConsumer<T, DeliveryContext> consumer) throws IOException {
        channel = manager.getConnection().createChannel();
        channel.exchangeDeclare(exchange, exchangeType);
        if (queueName == null) {
            queueName = channel.queueDeclare().getQueue();
        } else {
            channel.queueDeclare(queueName, true, false, false, null);
        }
        channel.queueBind(queueName, exchange, getRoutingKey());

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String messageJson = new String(delivery.getBody(), StandardCharsets.UTF_8);
            try {
                T message = MatrixCommon.GSON.fromJson(messageJson, messageClass);
                consumer.accept(message, new DeliveryContext(channel, delivery.getEnvelope().getDeliveryTag()));
            } catch (Exception e) {
                logger.error("Error processing RabbitMQ message", e);
                channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);
            }
        };

        channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {
        });
    }

    public static class DeliveryContext {

        private final Channel channel;
        private final long deliveryTag;

        public DeliveryContext(Channel channel, long deliveryTag) {
            this.channel = channel;
            this.deliveryTag = deliveryTag;
        }

        public void acknowledge() throws IOException {
            channel.basicAck(deliveryTag, false);
        }

        public void reject(boolean requeue) throws IOException {
            channel.basicReject(deliveryTag, requeue);
        }
    }

    protected abstract String getRoutingKey();

    public void shutdown() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
            }
        } catch (Exception e) {
            logger.error("Error closing RabbitMQ consumer channel", e);
        }
    }
}
