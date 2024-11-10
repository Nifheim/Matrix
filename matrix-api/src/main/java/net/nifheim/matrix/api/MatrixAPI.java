package net.nifheim.matrix.api;

import net.nifheim.matrix.api.database.MatrixDatabase;
import net.nifheim.matrix.api.environment.Environment;
import net.nifheim.matrix.api.messaging.MessagingService;
import net.nifheim.matrix.api.player.PlayerManager;
import net.nifheim.matrix.api.server.ServerInfo;
import net.nifheim.matrix.api.server.ServerManager;
import net.nifheim.matrix.api.service.MatrixService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * @author Jaime Suárez
 */
public interface MatrixAPI {

    /**
     * Get the SLF4J {@link Logger} instance in use by this {@link MatrixAPI} instance, it must be used for all internal
     * logging.
     *
     * @return current {@link Logger} instance.
     */
    Logger getLogger();

    /**
     * {@link MessagingService} service used by this {@link MatrixAPI} instance to communicate with other running matrix
     * instances, it may or may not be inactive.
     *
     * @return current {@link MessagingService} service instance.
     */
    @NotNull MessagingService getMessaging();

    /**
     * {@link MatrixDatabase} service used by this {@link MatrixAPI} instance to handle persistent data across different
     * matrix instances.
     *
     * @return current {@link MatrixDatabase} service instance.
     */
    @NotNull MatrixDatabase getDatabase();


    /**
     * Get the {@link Environment}, it may be used by implementations or other apps to identify servers where a feature
     * must be enabled or storing data based on the server environment.
     */
    @NotNull Environment getEnvironment();

    /**
     * <p>
     * Get {@link ServerInfo} for the server this matrix instance is running on.
     * </p>
     * <p>
     * Note that not every implementation must run on a minecraft server, so this method may return null when there is
     * no {@link ServerInfo} available.
     * </p>
     * <p>
     * This method is not intended to be used to get information about other servers, for that use
     * {@link ServerManager#getServer(String)}}.
     * This method is intended to be used to get information about the server this matrix instance is running on.
     * See {@link ServerInfo} for more information about the server information.
     * </p>
     * <p>
     * Note that this method may return null if the server information is not available.
     * This may happen if the implementation is not running on a minecraft server or minecraft proxy.
     * For example, if the implementation is running on a standalone application. Where there is no server where
     * minecraft players can interact with.
     * In that case, this method will return null.
     * You can check the {@link Environment} to know if the implementation is running on a minecraft server or not.
     * </p>
     *
     * @return {@link ServerInfo} for this server, or {@code null} if this implementation isn't a minecraft server.ç
     * @see ServerInfo
     * @see Environment
     * @see ServerManager
     */
    @Nullable ServerInfo getServerInfo();

    /**
     * Get the {@link ServerManager} instance in use by this implementation. It can be used to fetch information about
     * minecraft servers running with a matrix instance.
     *
     * @return current {@link ServerManager} instance.
     */
    @NotNull ServerManager getServerManager();

    /**
     * Get the {@link PlayerManager} instance in use by this implementation. It can be used to fetch information about
     * online and offline players, any update to {@link net.nifheim.matrix.api.player.MatrixPlayer}s obtained
     * using this service will be synced with other matrix instances connected to the same {@link MessagingService}.
     *
     * @return current {@link PlayerManager} instance.
     */
    @NotNull PlayerManager getPlayerManager();

    /**
     * Set or override a {@link MatrixService} used in the implementation, there may be a service that can't be
     * overridden for security reasons or because the implementation doesn't support it, in that case it will throw an
     * {@link UnsupportedOperationException}.
     *
     * @param service service to set or override.
     * @param <S>     {@link MatrixService} replacement.
     * @throws UnsupportedOperationException when the implementation can't set or override the new service.
     */
    <S extends MatrixService> void setService(Class<S> type, MatrixService service) throws UnsupportedOperationException;
}
