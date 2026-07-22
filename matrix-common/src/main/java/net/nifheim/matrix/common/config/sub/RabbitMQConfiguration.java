package net.nifheim.matrix.common.config.sub;

import org.jetbrains.annotations.NotNull;

public interface RabbitMQConfiguration {

    @NotNull String getHost();

    int getPort();

    @NotNull String getUsername();

    @NotNull String getPassword();

    @NotNull String getVirtualHost();
}
