package net.nifheim.matrix.common.config.adapter;

import net.nifheim.matrix.common.config.FileConfigurationWrapper;
import net.nifheim.matrix.common.config.sub.MariaDbConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Adapter class to convert a {@link FileConfigurationWrapper} to a {@link MariaDbConfiguration}.
 *
 * @author Jaime Suárez
 */
public class MariaDbConfigurationAdapter implements MariaDbConfiguration {

    private final FileConfigurationWrapper config;

    public MariaDbConfigurationAdapter(FileConfigurationWrapper config) {
        this.config = config;
    }

    @Override
    public @NotNull String getHost() {
        return config.getString("mariadb.host", "localhost");
    }

    @Override
    public int getPort() {
        return config.getInt("mariadb.port", 3306);
    }

    @Override
    public @NotNull String getDatabase() {
        return config.getString("mariadb.database", "matrix");
    }

    @Override
    public @NotNull String getUsername() {
        return config.getString("mariadb.username", "root");
    }

    @Override
    public @NotNull String getPassword() {
        return config.getString("mariadb.password", "password");
    }

    @Override
    public int getPoolSize() {
        return config.getInt("mariadb.pool-size", 8);
    }
}
