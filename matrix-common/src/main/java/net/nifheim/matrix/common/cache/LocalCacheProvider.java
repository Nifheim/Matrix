package net.nifheim.matrix.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.nifheim.matrix.api.cache.CacheProvider;
import net.nifheim.matrix.api.service.InactiveServiceException;
import net.nifheim.matrix.api.player.MatrixPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class LocalCacheProvider implements CacheProvider<MatrixPlayer> {

    private final Logger logger;
    private final Cache<UUID, MatrixPlayer> cachedPlayers = Caffeine.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).weakValues().build();

    public LocalCacheProvider(Logger logger) {
        this.logger = logger;
    }

    @Override
    public @NotNull Optional<MatrixPlayer> getPlayer(@NotNull UUID uniqueId) {
        return Optional.ofNullable(cachedPlayers.getIfPresent(uniqueId));
    }

    @Override
    public MatrixPlayer removePlayer(@NotNull MatrixPlayer player) {
        // TODO: check implementation of this and optimize, we shouldn't need a read, since the data is local
        MatrixPlayer cachedPlayer = cachedPlayers.getIfPresent(player.getUniqueId());
        if (cachedPlayer != null) {
            cachedPlayers.invalidate(cachedPlayer.getUniqueId());
        }
        return cachedPlayer;
    }

    @Override
    public boolean isCached(UUID uniqueId) {
        return cachedPlayers.getIfPresent(uniqueId) != null;
    }

    @Override
    public void updateCachedFieldById(@NotNull UUID uniqueId, @NotNull String field, @Nullable Object value) {
    }

    @Override
    public void add(@NotNull MatrixPlayer player) {
        cachedPlayers.put(player.getUniqueId(), player);
    }

    @Override
    public void update(@NotNull MatrixPlayer player) {
        if (isCached(player.getUniqueId())) {
            cachedPlayers.put(player.getUniqueId(), player);
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
