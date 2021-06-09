package com.github.beelzebu.matrix.database;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.cache.CacheProvider;
import com.github.beelzebu.matrix.api.database.MatrixDatabase;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.scheduler.SchedulerAdapter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public class MatrixDatabaseImpl implements MatrixDatabase {

    private final StorageProvider storage;
    private final CacheProvider cacheProvider;
    private final SchedulerAdapter schedulerAdapter;

    public MatrixDatabaseImpl(StorageProvider storage, CacheProvider cacheProvider, SchedulerAdapter schedulerAdapter) {
        this.storage = storage;
        this.cacheProvider = cacheProvider;
        this.schedulerAdapter = schedulerAdapter;
    }

    @Override
    public @NotNull CompletableFuture<MatrixPlayer> getPlayer(@NotNull UUID uniqueId) {
        return schedulerAdapter.makeFuture(() -> {
            Matrix.getLogger().debug("Requesting player by UUID " + uniqueId);
            return cacheProvider.getPlayer(uniqueId).orElseGet(() -> {
                Matrix.getLogger().debug("Player not found on cache, falling back to storage " + uniqueId);
                return storage.getPlayer(uniqueId);
            });
        });
    }

    @Override
    public @NotNull CompletableFuture<MatrixPlayer> getPlayerByName(@NotNull String name) {
        return schedulerAdapter.makeFuture(() -> {
            Matrix.getLogger().debug("Requesting player by name " + name);
            return cacheProvider.getPlayerByName(name).orElseGet(() -> {
                Matrix.getLogger().debug("Player not found on cache, falling back to storage " + name);
                return storage.getPlayerByName(name);
            });
        });
    }

    @Override
    public @NotNull CompletableFuture<MatrixPlayer> getPlayerById(@NotNull String hexId) {
        return schedulerAdapter.makeFuture(() -> cacheProvider.getPlayerById(hexId).orElse(storage.getPlayerById(hexId)));
    }

    @Override
    public @NotNull CompletableFuture<Boolean> isStored(@NotNull UUID uniqueId) {
        return schedulerAdapter.makeFuture(() -> {
            Matrix.getLogger().debug("Checking if player is stored by UUID " + uniqueId);
            Optional<MatrixPlayer> optionalPlayer = cacheProvider.getPlayer(uniqueId);
            if (optionalPlayer.isPresent()) {
                return true;
            } else {
                Matrix.getLogger().debug("Player not found on cache, falling back to storage " + uniqueId);
                return storage.getPlayer(uniqueId) != null;
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> isStoredByName(@NotNull String name) {
        return schedulerAdapter.makeFuture(() -> {
            Matrix.getLogger().debug("Checking if player is stored by name " + name);
            Optional<MatrixPlayer> optionalPlayer = cacheProvider.getPlayerByName(name);
            if (optionalPlayer.isPresent()) {
                return true;
            } else {
                Matrix.getLogger().debug("Player not found on cache, falling back to storage " + name);
                return storage.getPlayerByName(name) != null;
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> isStoredById(@NotNull String hexId) {
        return schedulerAdapter.makeFuture(() -> {
            Matrix.getLogger().debug("Checking if player is stored by id " + hexId);
            Optional<MatrixPlayer> optionalPlayer = cacheProvider.getPlayerById(hexId);
            if (optionalPlayer.isPresent()) {
                return true;
            } else {
                Matrix.getLogger().debug("Player not found on cache, falling back to storage " + hexId);
                return storage.getPlayerById(hexId) != null;
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> isRegistered(@NotNull UUID uniqueId) {
        return schedulerAdapter.makeFuture(() -> {
            boolean cached = cacheProvider.isCached(uniqueId);
            if (cached) {
                return true;
            }
            return storage.isRegistered(uniqueId);
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> isRegisteredByName(@NotNull String name) {
        return schedulerAdapter.makeFuture(() -> {
            boolean cached = cacheProvider.isCachedByName(name);
            if (cached) {
                return true;
            }
            return storage.isRegisteredByName(name);
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> isRegisteredById(@NotNull String hexId) {
        return schedulerAdapter.makeFuture(() -> {
            boolean cached = cacheProvider.isCachedById(hexId);
            if (cached) {
                return true;
            }
            return storage.isRegisteredById(hexId);
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> purgeForAllPlayers(@NotNull String field) {
        return schedulerAdapter.makeFuture(() -> storage.purgeForAllPlayers(field));
    }

    @Override
    public @NotNull CompletableFuture<Void> addFailedLogin(@NotNull UUID uniqueId, String server, @NotNull String message) {
        return schedulerAdapter.makeFuture(() -> storage.addFailedLogin(uniqueId, server, message));
    }

    @Override
    public @NotNull CompletableFuture<Void> insertCommandLogEntryById(String hexId, String server, String command) {
        return schedulerAdapter.makeFuture(() -> storage.insertCommandLogEntryById(hexId, server, command));
    }

    @Override
    public <T> @NotNull CompletableFuture<T> updateFieldById(String hexId, @NotNull String s, T t) {
        return schedulerAdapter.makeFuture(() -> cacheProvider.updateCachedFieldById(hexId, s, t));
    }

    @Override
    public <T extends MatrixPlayer> @NotNull CompletableFuture<Boolean> save(@Nullable String hexId, @NotNull T mongoMatrixPlayer) {
        return schedulerAdapter.makeFuture(() -> {
            try {
                if (hexId != null) {
                    cacheProvider.update(mongoMatrixPlayer.getName(), mongoMatrixPlayer.getUniqueId(), hexId);
                }
                cacheProvider.saveToCache(storage.save(mongoMatrixPlayer));
            } catch (Exception e) {
                Matrix.getLogger().debug(e);
                return false;
            }
            return true;
        });
    }

    @Override
    public void cleanUp(@NotNull MatrixPlayer matrixPlayer) {
        schedulerAdapter.makeFuture(() -> cacheProvider.removePlayer(matrixPlayer));
    }

    @Override
    public void shutdown() {
        cacheProvider.shutdown();
    }

    @Override
    public @NotNull CacheProvider getCacheProvider() {
        return cacheProvider;
    }

    public StorageProvider getStorage() {
        return storage;
    }
}
