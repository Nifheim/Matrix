package net.nifheim.matrix.common.messaging;

import java.util.HashMap;
import java.util.Map;
import net.nifheim.matrix.api.messaging.Message;
import net.nifheim.matrix.api.service.InactiveServiceException;
import net.nifheim.matrix.api.service.MatrixService;
import net.nifheim.matrix.common.messaging.rabbitmq.AbstractRabbitMQConsumer;
import net.nifheim.matrix.common.messaging.rabbitmq.AbstractRabbitMQProducer;

public class MessagingService implements MatrixService {

    private final Map<Class<? extends AbstractRabbitMQProducer<? extends Message>>, AbstractRabbitMQProducer<? extends Message>> producers = new HashMap<>();
    private final Map<Class<? extends AbstractRabbitMQConsumer<? extends Message>>, AbstractRabbitMQConsumer<? extends Message>> consumers = new HashMap<>();

    public <M extends Message, T extends AbstractRabbitMQProducer<M>> void registerProducer(Class<T> clazz, T producer) {
        producers.put(clazz, producer);
    }

    public <M extends Message, T> T getProducer(Class<T> clazz) {
        return clazz.cast(producers.get(clazz));
    }

    public <M extends Message, T extends AbstractRabbitMQConsumer<M>> void registerConsumer(Class<T> clazz, T consumer) {
        consumers.put(clazz, consumer);
    }

    public <M extends Message, T> T getConsumer(Class<T> clazz) {
        return clazz.cast(consumers.get(clazz));
    }

    @Override
    public void shutdown() throws InactiveServiceException {
        consumers.values().forEach(AbstractRabbitMQConsumer::shutdown);
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
