package net.nifheim.matrix.common.player;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.identity.Identified;
import net.nifheim.matrix.api.cache.CacheProvider;
import net.nifheim.matrix.api.messaging.MessagingService;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.player.PlayerManager;
import net.nifheim.matrix.api.server.ServerManager;
import net.nifheim.matrix.common.cache.UnifiedCacheProvider;
import net.nifheim.matrix.common.database.MatrixDatabaseImpl;
import net.nifheim.matrix.common.player.meta.PlayerMetaInjector;
import net.nifheim.matrix.common.scheduler.SchedulerAdapter;
import net.nifheim.matrix.common.util.RedisManager;
import net.nifheim.matrix.common.util.ReflectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * An implementation of the PlayerManager interface.
 */
public final class PlayerManagerImpl <P extends Identified> implements PlayerManager {

    public static final Map<String, Field> FIELDS;

    static {
        try {
            FIELDS = ReflectionUtils.decodeFields(MongoMatrixPlayer.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private final Logger logger;
    private final SchedulerAdapter schedulerAdapter;
    private final MatrixDatabaseImpl database;
    private final PlayerMetaInjector<P> playerMetaInjector;
    private final PlayerProxy<P> playerProxy;
    private final UnifiedCacheProvider cacheProvider;

    public PlayerManagerImpl(Logger logger, SchedulerAdapter schedulerAdapter, MatrixDatabaseImpl database, RedisManager redisManager, MessagingService messaging, ServerManager serverManager, PlayerMetaInjector<P> playerMetaInjector, PlayerProxy<P> playerProxy) {
        this.logger = logger;
        this.schedulerAdapter = schedulerAdapter;
        this.database = database;
        this.playerMetaInjector = playerMetaInjector;
        this.playerProxy = playerProxy;
        this.cacheProvider = new UnifiedCacheProvider(logger, redisManager, messaging, serverManager);
    }

    public PlayerMetaInjector<P> getMetaInjector() {
        return playerMetaInjector;
    }

    public @Nullable P getPlatformPlayer(UUID uniqueId) {
        return playerProxy.getPlatformPlayer(uniqueId);
    }

    @Override
    public @NotNull CompletableFuture<@Nullable MatrixPlayer> getPlayerById(@NotNull String hexId) {
        return schedulerAdapter.makeFuture(() -> getPlayerByIdSync(hexId));
    }

    @Override
    public @NotNull CompletableFuture<@Nullable MatrixPlayer> getPlayerByUniqueId(UUID uniqueId) {
        return schedulerAdapter.makeFuture(() -> getPlayerByUniqueIdSync(uniqueId));
    }

    @Override
    public void disconnect(@NotNull MatrixPlayer player) {
        if (player instanceof MongoMatrixPlayer mongoPlayer) {
            MongoMatrixPlayer cachedPlayer = cacheProvider.removePlayer(mongoPlayer);
            if (cachedPlayer != null) {
                cachedPlayer = database.save(cachedPlayer);
                database.saveLogout(cachedPlayer);
            }
            return;
        }
        throw new IllegalArgumentException("Player is not an instance of MongoMatrixPlayer");
    }

    @NotNull
    @Override
    public UnifiedCacheProvider getCacheProvider() {
        return cacheProvider;
    }

    @Override
    public void updateProperty(MatrixPlayer player, String property, Object value) throws ReflectiveOperationException {
        Field field = FIELDS.get(property);
        ReflectionUtils.setField(player, field, value);
    }

    @Override
    public void propagateUpdate(MatrixPlayer matrixPlayer) {
        if (matrixPlayer instanceof MongoMatrixPlayer player) {
            if (!cacheProvider.isCached(player.getId())) {
                logger.info("Saving player {} ({} - {}) to cache during propagation", player.getName(), player.getUniqueId(), player.getId());
                cacheProvider.update(player);
            }
            if (!player.isDirty()) {
                return;
            }
            // update all different fields
            for (String name : player.$dirtyFields) {
                Field field = FIELDS.get(name);
                try {
                    Object value = field.get(player);
                    cacheProvider.updateCachedFieldById(player.getId(), name, value);
                } catch (ReflectiveOperationException e) {
                    logger.error("An exception has occurred while saving player", e);
                }
            }
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> save(MatrixPlayer player) {
        return schedulerAdapter.makeFuture(() -> saveSync(player));
    }

    public void saveSync(MatrixPlayer matrixPlayer) {
        if (matrixPlayer instanceof MongoMatrixPlayer player) {
            propagateUpdate(player);
            // store the last cached data in the database
            // TODO: check if we need to expose the redis cached version to save to the database
            cacheProvider.getPlayer(player.getId()).ifPresent(database::save);
        }
    }

    public @Nullable MongoMatrixPlayer getPlayerByIdSync(String hexId) {
        Optional<MongoMatrixPlayer> optionalPlayer = cacheProvider.getPlayer(hexId);
        if (optionalPlayer.isPresent()) {
            return optionalPlayer.get();
        }
        logger.debug("Player {} not found in cache, searching in storage", hexId);
        return database.getPlayer(hexId);
    }

    public @Nullable MongoMatrixPlayer getPlayerByUniqueIdSync(UUID uniqueId) {
        String hexId = getHexId(uniqueId);
        if (hexId != null) {
            Optional<MongoMatrixPlayer> optionalPlayer = cacheProvider.getPlayer(hexId);
            if (optionalPlayer.isPresent()) {
                return optionalPlayer.get();
            }
        }
        logger.debug("Player {} not found in cache, searching in storage", uniqueId);
        return database.getPlayer(uniqueId);
    }

    public @Nullable MongoMatrixPlayer getPlayerByNameSync(String name) {
        return database.getPlayerByName(name);
    }

    public void loginSync(MongoMatrixPlayer player, InetAddress address) {
        cacheProvider.update(player.getUniqueId(), player.getId());
        cacheProvider.add(player);
        database.saveAddress(player, address);
        database.saveLogin(player);
        saveSync(player);
    }

    /**
     * Get the HexId for a player, the order of search is: player meta, then cache.
     *
     * @param uniqueId the uniqueId of the player
     * @return the HexId of the player
     */
    public @Nullable String getHexId(UUID uniqueId) {
        P player = playerProxy.getPlatformPlayer(uniqueId);
        if (player != null) {
            return playerMetaInjector.getId(player);
        }
        return cacheProvider.getHexId(uniqueId).orElse(null);
    }
}
