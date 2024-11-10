package net.nifheim.matrix.common.plugin;

import com.google.gson.GsonBuilder;
import java.util.Date;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identified;
import net.nifheim.matrix.api.database.MatrixDatabase;
import net.nifheim.matrix.api.environment.EnvironmentType;
import net.nifheim.matrix.api.messaging.MessagingService;
import net.nifheim.matrix.api.server.ServerInfo;
import net.nifheim.matrix.common.api.MatrixCommon;
import net.nifheim.matrix.common.config.MatrixConfiguration;
import net.nifheim.matrix.common.database.MatrixDatabaseImpl;
import net.nifheim.matrix.common.messaging.RedisMessaging;
import net.nifheim.matrix.common.messaging.message.ServerUnregisterMessage;
import net.nifheim.matrix.common.player.PlayerManagerImpl;
import net.nifheim.matrix.common.player.PlayerProxy;
import net.nifheim.matrix.common.player.meta.PlayerMetaInjector;
import net.nifheim.matrix.common.server.ServerInfoImpl;
import net.nifheim.matrix.common.server.ServerManagerImpl;
import net.nifheim.matrix.common.util.MaintenanceManager;
import net.nifheim.matrix.common.util.RedisManager;
import net.nifheim.matrix.common.util.adapter.DateTypeAdapter;
import net.nifheim.matrix.common.util.adapter.ObjectIdTypeAdapter;
import net.nifheim.matrix.common.util.adapter.ServerInfoTypeAdapter;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

public final class MatrixPluginCommon <P extends Identified> {

    private final @NotNull MatrixBootstrap<P> bootstrap;
    private final @NotNull MatrixConfiguration configuration;
    private final @NotNull PlayerMetaInjector<P> playerMetaInjector;
    private final @NotNull PlayerProxy<P> playerProxy;
    // TODO: messages should be moved to resourcepack and sent to clients, maybe a enum should be used to identify the message keys
    //private final Map<String, AbstractConfig> messagesMap = new HashMap<>();
    private MatrixDatabaseImpl database;
    private RedisManager redisManager;
    private RedisMessaging messaging;
    private MaintenanceManager maintenanceManager;
    private ServerManagerImpl serverManager;
    private PlayerManagerImpl<P> playerManager;
    private final MatrixCommon<P> api;

    public MatrixPluginCommon(@NotNull MatrixBootstrap<P> bootstrap, @NotNull MatrixConfiguration configuration, @NotNull PlayerMetaInjector<P> playerMetaInjector, @NotNull PlayerProxy<P> playerProxy) {
        this.bootstrap = bootstrap;
        this.configuration = configuration;
        this.playerMetaInjector = playerMetaInjector;
        this.playerProxy = playerProxy;
        this.api = new MatrixCommon<>(this);
    }

    public @NotNull PlayerManagerImpl<P> getPlayerManager() {
        return playerManager;
    }

    public String getVersion() {
        return bootstrap.getVersion();
    }

    public void load() {
        // setup gson serializer
        MatrixCommon.GSON = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .registerTypeAdapter(ObjectId.class, new ObjectIdTypeAdapter())
                .registerTypeAdapter(ServerInfoImpl.class, new ServerInfoTypeAdapter())
                .registerTypeAdapter(ServerInfo.class, new ServerInfoTypeAdapter())
                .create();
    }

    /**
     * Set up the plugin and all its services, including database, redis, messaging, cache, server manager and api.
     * This method should be called after the plugin has been enabled.
     *
     * @throws Exception if any of the services could not be initialized
     */
    public void enable() throws Exception {
        // setup redis connection for messaging, server discovery and caching services
        api.getLogger().info("Initializing redis manager...");
        try {
            redisManager = new RedisManager(configuration.getRedisConfig(), api.getLogger());
            api.getLogger().info("Redis manager initialized!");
        } catch (Exception e) {
            api.getLogger().info("Could not connect to Redis server");
            // TODO: add generic disable method
            throw e;
        }
        // setup messaging service
        api.getLogger().info("Initializing messaging service...");
        try {
            messaging = new RedisMessaging(redisManager, bootstrap.getPlatformLogger(), bootstrap.getScheduler()::executeAsync);
            api.setService(MessagingService.class, messaging);
            api.getLogger().info("Messaging service initialized!");
        } catch (Exception e) {
            api.getLogger().info("Could not connect to Redis server");
            throw e;
        }
        // setup server manager before cache because when the cache service is shutdown we should always remove the server from the server manager
        api.getLogger().info("Initializing server manager...");
        serverManager = new ServerManagerImpl(bootstrap.getScheduler(), redisManager, configuration.getServerInfo(), bootstrap.getServerPlatformInfo(), bootstrap.getPlatformLogger());
        api.getLogger().info("Server manager initialized!");
        api.getLogger().info("Initializing database manager..");
        try {
            database = new MatrixDatabaseImpl(getConfiguration(), api.getLogger());
            api.setService(MatrixDatabase.class, database);
            api.getLogger().info("Database manager initialized!");
        } catch (Exception e) {
            api.getLogger().info("Could not connect to database");
            throw e;
        }
        // TODO: move cache initialization from player manager to here
        api.getLogger().info("Initializing player manager...");
        playerManager = new PlayerManagerImpl<>(bootstrap.getPlatformLogger(), bootstrap.getScheduler(), database, redisManager, messaging, serverManager, playerMetaInjector, playerProxy);
        api.getLogger().info("Player manager initialized!");
        api.getLogger().info("Initializing maintenance manager...");
        maintenanceManager = new MaintenanceManager(redisManager, bootstrap.getPlatformLogger());
        api.getLogger().info("Maintenance manager initialized!");
        api.getLogger().info("Server info created!");
        api.getLogger().info("Matrix API is now fully initialized.");
        api.setup();
    }

    /**
     * Shutdown the plugin and all its services, including database, redis, messaging, cache, server manager and api.
     */
    public void disable() {
        // send server unregister message
        if (serverManager != null) {
            if (bootstrap.getEnvironment().environmentType() == EnvironmentType.MINECRAFT_SERVER) {
                messaging.sendMessage(new ServerUnregisterMessage(serverManager.getServerInfo()));
            }
        }
        // shutdown all services
        api.getLogger().info("Shutting down Matrix API...");
        api.shutdown();
        api.getLogger().info("Shutting down cache service...");
        if (database != null) {
            database.shutdown();
        }
        api.getLogger().info("Shutting down messaging service...");
        if (messaging != null) {
            messaging.shutdown();
        }
        api.getLogger().info("Shutting down redis manager...");
        if (redisManager != null) {
            redisManager.shutdown();
        }
        api.getLogger().info("Shutting down scheduler...");
        bootstrap.getScheduler();
        bootstrap.getScheduler().shutdownScheduler();
        bootstrap.getScheduler().shutdownExecutor();
        api.getLogger().info("Matrix API is now disabled.");
    }

    public @NotNull MatrixConfiguration getConfiguration() {
        return configuration;
    }

    //public Map<String, AbstractConfig> getMessagesMap() {
    //    return messagesMap;
    //}

    public MatrixDatabaseImpl getDatabase() {
        return database;
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public RedisMessaging getMessaging() {
        return messaging;
    }

    public ServerInfo getServerInfo() {
        return serverManager.getServerInfo();
    }

    public MaintenanceManager getMaintenanceManager() {
        return maintenanceManager;
    }

    public ServerManagerImpl getServerManager() {
        return serverManager;
    }

    public MatrixCommon<P> getApi() {
        return api;
    }

    public Audience getConsole() {
        return bootstrap.getConsole();
    }

    public @NotNull MatrixBootstrap getBootstrap() {
        return bootstrap;
    }
}
