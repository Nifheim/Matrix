package net.nifheim.matrix.common.config.sub;

import org.jetbrains.annotations.NotNull;

public interface MariaDbConfiguration {

    @NotNull String getHost();

    int getPort();

    @NotNull String getDatabase();

    @NotNull String getUsername();

    @NotNull String getPassword();

    int getPoolSize();
}
