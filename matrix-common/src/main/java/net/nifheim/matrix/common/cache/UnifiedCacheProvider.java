package net.nifheim.matrix.common.cache;

import java.util.Optional;
import java.util.UUID;
import net.nifheim.matrix.api.cache.CacheProvider;
import net.nifheim.matrix.api.messaging.MessagingService;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.server.ServerManager;
import net.nifheim.matrix.common.player.MongoMatrixPlayer;
import net.nifheim.matrix.common.util.RedisManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Represents a cache that has an underlying implementation composed of a local cache and a redis cache and uses both caches to store and provide the cached information, using the locally cached
 * version if exists first, then fetching it from the remote cache.
 *
 * @author Jaime Suárez
 */
public class UnifiedCacheProvider implements CacheProvider<MongoMatrixPlayer> {

    private final LocalCacheProvider localCacheProvider;
    private final RedisCacheProvider redisCacheProvider;

    public UnifiedCacheProvider(Logger logger, RedisManager redisManager, MessagingService messaging, ServerManager serverManager) {
        this.localCacheProvider = new LocalCacheProvider(logger);
        this.redisCacheProvider = new RedisCacheProvider(logger, redisManager, messaging, serverManager);
    }

    @Override
    public @NotNull Optional<@NotNull String> getHexId(@NotNull UUID uniqueId) {
        Optional<String> hexId = localCacheProvider.getHexId(uniqueId);
        if (hexId.isPresent()) {
            return hexId;
        }
        return redisCacheProvider.getHexId(uniqueId);
    }

    @Override
    public @NotNull Optional<UUID> getUniqueId(String hexId) {
        Optional<UUID> uniqueId = localCacheProvider.getUniqueId(hexId);
        if (uniqueId.isPresent()) {
            return uniqueId;
        }
        return redisCacheProvider.getUniqueId(hexId);
    }

    @Override
    public void update(@NotNull UUID uniqueId, @NotNull String hexId) {
        localCacheProvider.update(uniqueId, hexId);
        redisCacheProvider.update(uniqueId, hexId);
    }

    @Override
    public @NotNull Optional<MongoMatrixPlayer> getPlayer(@NotNull String hexId) {
        Optional<MongoMatrixPlayer> player = localCacheProvider.getPlayer(hexId);
        if (player.isPresent()) {
            return player;
        }
        return redisCacheProvider.getPlayer(hexId);
    }

    @Override
    public MongoMatrixPlayer removePlayer(@NotNull MongoMatrixPlayer player) {
        MongoMatrixPlayer cachedPlayer = localCacheProvider.removePlayer(player);
        return cachedPlayer != null ? cachedPlayer : player;
    }

    @Override
    public boolean isCached(@NotNull String hexId) {
        return localCacheProvider.isCached(hexId) && redisCacheProvider.isCached(hexId);
    }

    @Override
    public void updateCachedFieldById(@NotNull String hexId, @NotNull String field, @Nullable Object value) {
        localCacheProvider.updateCachedFieldById(hexId, field, value);
        redisCacheProvider.updateCachedFieldById(hexId, field, value);
    }

    @Override
    public void add(@NotNull MongoMatrixPlayer matrixPlayer) {
        localCacheProvider.add(matrixPlayer);
        redisCacheProvider.add(matrixPlayer);
    }

    @Override
    public void update(@NotNull MongoMatrixPlayer matrixPlayer) {
        localCacheProvider.update(matrixPlayer);
        redisCacheProvider.update(matrixPlayer);
    }

    @Override
    public void shutdown() {
        localCacheProvider.shutdown();
        redisCacheProvider.shutdown();
    }

    @Override
    public boolean isActive() {
        return localCacheProvider.isActive() && redisCacheProvider.isActive();
    }

    public Optional<MongoMatrixPlayer> getLocallyCached(String hexId) {
        return localCacheProvider.getPlayer(hexId);
    }
}
