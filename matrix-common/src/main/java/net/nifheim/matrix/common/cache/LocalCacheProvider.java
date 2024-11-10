package net.nifheim.matrix.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.nifheim.matrix.api.cache.CacheProvider;
import net.nifheim.matrix.api.service.InactiveServiceException;
import net.nifheim.matrix.common.player.MongoMatrixPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class LocalCacheProvider implements CacheProvider<MongoMatrixPlayer> {

    private final Logger logger;
    private final Cache<String, MongoMatrixPlayer> cachedPlayers = Caffeine.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).weakValues().build();

    public LocalCacheProvider(Logger logger) {
        this.logger = logger;
    }

    @Override
    public @NotNull Optional<UUID> getUniqueId(String hexId) {
        if (isCached(hexId)) {
            MongoMatrixPlayer player = cachedPlayers.getIfPresent(hexId);
            if (player != null) {
                return Optional.of(player.getUniqueId());
            }
        }
        return Optional.empty();
    }

    @Override
    public @NotNull Optional<String> getHexId(@NotNull UUID uniqueId) {
        // TODO: noop
        // implement search login on local cache
        return Optional.empty();
    }

    @Override
    public void update(@NotNull UUID uniqueId, @NotNull String hexId) {
        // NOOP
    }

    @Override
    public @NotNull Optional<MongoMatrixPlayer> getPlayer(@NotNull String hexId) {
        return Optional.ofNullable(cachedPlayers.getIfPresent(hexId));
    }

    @Override
    public MongoMatrixPlayer removePlayer(@NotNull MongoMatrixPlayer player) {
        // TODO: check implementation of this and optimize, we shouldn't need a read, since the data is local
        MongoMatrixPlayer cachedPlayer = cachedPlayers.getIfPresent(player.getId());
        if (cachedPlayer != null) {
            cachedPlayers.invalidate(cachedPlayer.getId());
        }
        return cachedPlayer;
    }

    @Override
    public boolean isCached(@NotNull String hexId) {
        return cachedPlayers.getIfPresent(hexId) != null;
    }

    @Override
    public void updateCachedFieldById(@NotNull String hexId, @NotNull String field, @Nullable Object value) {

    }

    @Override
    public void add(@NotNull MongoMatrixPlayer matrixPlayer) {
        cachedPlayers.put(matrixPlayer.getId(), matrixPlayer);
    }

    @Override
    public void update(@NotNull MongoMatrixPlayer matrixPlayer) {
        if (isCached(matrixPlayer.getId())) {
            cachedPlayers.put(matrixPlayer.getId(), matrixPlayer);
        }
    }

    @Override
    public void shutdown() throws InactiveServiceException {
        // NOOP
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
