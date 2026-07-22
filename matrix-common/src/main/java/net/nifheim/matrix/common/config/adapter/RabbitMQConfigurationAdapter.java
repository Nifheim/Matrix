package net.nifheim.matrix.common.config.adapter;

import net.nifheim.matrix.common.config.FileConfigurationWrapper;
import net.nifheim.matrix.common.config.sub.RabbitMQConfiguration;
import org.jetbrains.annotations.NotNull;

public class RabbitMQConfigurationAdapter implements RabbitMQConfiguration {

    private final FileConfigurationWrapper config;

    public RabbitMQConfigurationAdapter(FileConfigurationWrapper config) {
        this.config = config;
    }

    @Override
    public @NotNull String getHost() {
        return config.getString("rabbitmq.host", "localhost");
    }

    @Override
    public int getPort() {
        return config.getInt("rabbitmq.port", 5672);
    }

    @Override
    public @NotNull String getUsername() {
        return config.getString("rabbitmq.username", "guest");
    }

    @Override
    public @NotNull String getPassword() {
        return config.getString("rabbitmq.password", "");
    }

    @Override
    public @NotNull String getVirtualHost() {
        return config.getString("rabbitmq.virtual-host", "/");
    }
}
