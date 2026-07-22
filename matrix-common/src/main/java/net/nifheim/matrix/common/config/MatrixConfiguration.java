package net.nifheim.matrix.common.config;

import net.nifheim.matrix.common.config.sub.MariaDbConfiguration;
import net.nifheim.matrix.common.config.sub.RabbitMQConfiguration;
import net.nifheim.matrix.common.config.sub.RedisConfiguration;
import net.nifheim.matrix.common.config.sub.ServerInfoConfiguration;

/**
 * Represents the configuration file of the Matrix plugin. It stores the configuration for connecting to the databases
 * and the server information.
 *
 * @author Jaime Suárez
 */
public interface MatrixConfiguration {

    MariaDbConfiguration getMariaDbConfig();

    RedisConfiguration getRedisConfig();

    RabbitMQConfiguration getRabbitMQConfig();

    ServerInfoConfiguration getServerInfo();

    boolean isDebug();

    void reload();
}
