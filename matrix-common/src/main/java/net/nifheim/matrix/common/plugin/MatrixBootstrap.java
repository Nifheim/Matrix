package net.nifheim.matrix.common.plugin;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.nifheim.matrix.api.environment.Environment;
import net.nifheim.matrix.common.scheduler.SchedulerAdapter;
import net.nifheim.matrix.common.server.ServerPlatformInfo;
import org.slf4j.Logger;

/**
 * MatrixBootstrap represents the minecraft server that is running the plugin.
 * <p>
 * This interface is implemented by the plugin and is used by the API to get information about the server.
 * It is also used to register listeners and tasks.
 * </p>
 *
 * @author Jaime Suárez
 * @see MatrixPluginCommon
 */
public interface MatrixBootstrap <P extends Identified> {


    /**
     * Get the SLF4J {@link Logger} instance in use by this {@link MatrixBootstrap} instance, it must be used for all internal
     * logging.
     *
     * @return current {@link Logger} instance.
     */
    Logger getPlatformLogger();

    /**
     * Get the {@link SchedulerAdapter} instance in use by this {@link MatrixBootstrap} instance, it must be used for all
     * internal scheduling.
     *
     * @return current {@link SchedulerAdapter} instance.
     */
    SchedulerAdapter getScheduler();

    /**
     * Get the {@link Environment}, it may be used by implementations or other apps to identify servers where a feature
     * must be enabled or storing data based on the server environment.
     *
     * @return current {@link Environment} instance.
     */
    Environment getEnvironment();

    /**
     * Get the {@link Audience} for the console, it may be used to send messages to the console.
     *
     * @return current {@link Audience} instance.
     */
    Audience getConsole();

    /**
     * Get the version of the plugin, it may be used to identify the version of the plugin running in the server.
     *
     * @return current version of the plugin.
     */
    String getVersion();

    /**
     * Get the {@link MatrixPluginCommon} instance in use by this {@link MatrixBootstrap} instance, it must be used for all
     * internal plugin operations.
     *
     * @return current {@link MatrixPluginCommon} instance.
     */
    MatrixPluginCommon<P> getPlugin();

    /**
     * Get the {@link ServerPlatformInfo} instance in use by this {@link MatrixBootstrap} instance, it must be used for all
     * internal server operations.
     *
     * @return current {@link ServerPlatformInfo} instance.
     */
    ServerPlatformInfo getServerPlatformInfo();
}
