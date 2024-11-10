package net.nifheim.matrix.common.config.adapter;

import net.nifheim.matrix.common.config.FileConfigurationWrapper;
import net.nifheim.matrix.common.config.sub.MongoConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Adapter class to convert a {@link FileConfigurationWrapper} to a {@link MongoConfiguration}.
 *
 * @author Jaime Suárez
 */
public class MongoConfigurationAdapter implements MongoConfiguration {

    private final FileConfigurationWrapper config;

    public MongoConfigurationAdapter(FileConfigurationWrapper config) {
        this.config = config;
    }

    @Override
    public @NotNull String getHost() {
        return config.getString("mongo.host", "localhost");
    }

    @Override
    public int getPort() {
        return config.getInt("mongo.port", 27017);
    }

    @Override
    public @NotNull String getDatabase() {
        return config.getString("mongo.database", "matrix");
    }

    @Override
    public @NotNull String getUsername() {
        return config.getString("mongo.username", "root");
    }

    @Override
    public @NotNull String getPassword() {
        return config.getString("mongo.password", "password");
    }
}
