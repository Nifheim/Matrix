package net.nifheim.matrix.common.messaging.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import net.nifheim.matrix.api.service.MatrixService;
import net.nifheim.matrix.common.config.sub.RabbitMQConfiguration;
import org.slf4j.Logger;

public class RabbitMQService implements MatrixService {

    private final Connection connection;
    private final Logger logger;

    public RabbitMQService(RabbitMQConfiguration configuration, Logger logger) {
        this.logger = logger;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(configuration.getHost());
        factory.setPort(configuration.getPort());
        factory.setUsername(configuration.getUsername());
        factory.setPassword(configuration.getPassword());
        factory.setVirtualHost(configuration.getVirtualHost());
        factory.setAutomaticRecoveryEnabled(true);

        try {
            this.connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            logger.error("Could not connect to RabbitMQ", e);
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void shutdown() {
        try {
            if (connection != null && connection.isOpen()) {
                connection.close();
            }
        } catch (IOException e) {
            logger.error("Error closing RabbitMQ connection", e);
        }
    }

    @Override
    public boolean isActive() {
        return connection != null && connection.isOpen();
    }
}
