package net.nifheim.matrix.common.player;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.identity.Identified;
import net.nifheim.matrix.api.cache.CacheProvider;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.player.PlayerManager;
import net.nifheim.matrix.common.cache.LocalCacheProvider;
import net.nifheim.matrix.common.database.MatrixDatabaseImpl;
import net.nifheim.matrix.common.scheduler.SchedulerAdapter;
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
            FIELDS = ReflectionUtils.decodeFields(MatrixPlayer.class);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private final Logger logger;
    private final SchedulerAdapter schedulerAdapter;
    private final MatrixDatabaseImpl database;
    private final PlayerProxy<P> playerProxy;
    private final CacheProvider<MatrixPlayer> cacheProvider;

    public PlayerManagerImpl(Logger logger, SchedulerAdapter schedulerAdapter, MatrixDatabaseImpl database, PlayerProxy<P> playerProxy) {
        this.logger = logger;
        this.schedulerAdapter = schedulerAdapter;
        this.database = database;
        this.playerProxy = playerProxy;
        this.cacheProvider = new LocalCacheProvider(logger);
    }


    public @Nullable P getPlatformPlayer(UUID uniqueId) {
        return playerProxy.getPlatformPlayer(uniqueId);
    }

    @Override
    public @NotNull CompletableFuture<@Nullable MatrixPlayer> getPlayer(UUID uniqueId) {
        return schedulerAdapter.makeFuture(() -> getPlayerSync(uniqueId));
    }

    @Override
    public @Nullable MatrixPlayer getPlayerSync(@NotNull UUID uniqueId) {
        return cacheProvider.getPlayer(uniqueId).orElseGet(() -> {
            logger.warn("Player {} not found in cache, searching in storage", uniqueId);
            return database.getPlayer(uniqueId);
        });
    }

    @Override
    public @Nullable MatrixPlayer getPlayerSync(@NotNull String name) {
        logger.debug("Player {} not found in cache, searching in storage", name);
        return database.getPlayerByName(name);
    }

    @Override
    public @NotNull MatrixPlayer createPlayer(@NotNull UUID uniqueId, @NotNull String name, @Nullable Locale locale) {
        return database.createPlayer(uniqueId, name, locale);
    }

    @NotNull
    @Override
    public CacheProvider<MatrixPlayer> getCacheProvider() {
        return cacheProvider;
    }

    @Override
    public void updateProperty(MatrixPlayer player, String property, Object value) throws ReflectiveOperationException {
        Field field = FIELDS.get(property);
        ReflectionUtils.setField(player, field, value);
    }

    @Override
    public @NotNull CompletableFuture<Void> save(MatrixPlayer player) {
        return schedulerAdapter.makeFuture(() -> saveSync(player));
    }

    public void saveSync(MatrixPlayer player) {
        // store the last cached data in the database
        // TODO: check if we need to expose the redis cached version to save to the database
        cacheProvider.getPlayer(player.getUniqueId()).ifPresent(database::save);
    }
}
