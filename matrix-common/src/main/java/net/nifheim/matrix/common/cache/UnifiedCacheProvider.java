package net.nifheim.matrix.common.cache;

import java.util.Optional;
import java.util.UUID;
import net.nifheim.matrix.api.cache.CacheProvider;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.server.ServerManager;
import net.nifheim.matrix.common.messaging.MessagingService;
import net.nifheim.matrix.common.util.RedisManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Represents a cache that has an underlying implementation composed of a local cache and a redis cache and uses both
 * caches to store and provide the cached information, using the locally cached version if exists first, then fetching
 * it from the remote cache.
 *
 * @author Jaime Suárez
 */
public class UnifiedCacheProvider implements CacheProvider<MatrixPlayer> {

    private final LocalCacheProvider localCacheProvider;
    private final RedisCacheProvider redisCacheProvider;

    public UnifiedCacheProvider(Logger logger, RedisManager redisManager, MessagingService messaging, ServerManager serverManager) {
        this.localCacheProvider = new LocalCacheProvider(logger);
        this.redisCacheProvider = new RedisCacheProvider(logger, redisManager, messaging, serverManager);
    }

    @Override
    public @NotNull Optional<MatrixPlayer> getPlayer(@NotNull UUID uniqueId) {
        Optional<MatrixPlayer> player = localCacheProvider.getPlayer(uniqueId);
        if (player.isPresent()) {
            return player;
        }
        return redisCacheProvider.getPlayer(uniqueId);
    }

    @Override
    public MatrixPlayer removePlayer(@NotNull MatrixPlayer player) {
        MatrixPlayer cachedPlayer = localCacheProvider.removePlayer(player);
        return cachedPlayer != null ? cachedPlayer : player;
    }

    @Override
    public boolean isCached(UUID uniqueId) {
        return localCacheProvider.isCached(uniqueId) && redisCacheProvider.isCached(uniqueId);
    }

    @Override
    public void updateCachedFieldById(@NotNull UUID uniqueId, @NotNull String field, @Nullable Object value) {
        localCacheProvider.updateCachedFieldById(uniqueId, field, value);
        redisCacheProvider.updateCachedFieldById(uniqueId, field, value);
    }

    @Override
    public void add(@NotNull MatrixPlayer player) {
        localCacheProvider.add(player);
        redisCacheProvider.add(player);
    }

    @Override
    public void update(@NotNull MatrixPlayer player) {
        localCacheProvider.update(player);
        redisCacheProvider.update(player);
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
}
