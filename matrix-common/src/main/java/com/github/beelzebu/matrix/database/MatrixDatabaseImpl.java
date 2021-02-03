package com.github.beelzebu.matrix.database;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.cache.CacheProvider;
import com.github.beelzebu.matrix.api.database.MatrixDatabase;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayStats;
import com.github.beelzebu.matrix.api.player.Statistic;
import com.github.beelzebu.matrix.api.player.TopEntry;
import com.github.beelzebu.matrix.api.scheduler.SchedulerAdapter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class MatrixDatabaseImpl implements MatrixDatabase {

    private final StorageImpl storage;
    private final CacheProvider cacheProvider;
    private final SchedulerAdapter schedulerAdapter;

    public MatrixDatabaseImpl(StorageImpl storage, CacheProvider cacheProvider, SchedulerAdapter schedulerAdapter) {
        this.storage = storage;
        this.cacheProvider = cacheProvider;
        this.schedulerAdapter = schedulerAdapter;
    }

    @Override
    public CompletableFuture<MatrixPlayer> getPlayer(@NotNull UUID uniqueId) {
        return schedulerAdapter.makeFuture(() -> {
            Matrix.getLogger().debug("Requesting player by UUID " + uniqueId);
            return cacheProvider.getPlayer(uniqueId).orElseGet(() -> {
                Matrix.getLogger().debug("Player not found on cache, falling back to storage " + uniqueId);
                return storage.getPlayer(uniqueId);
            });
        });
    }

    @Override
    public CompletableFuture<MatrixPlayer> getPlayerByName(@NotNull String name) {
        return schedulerAdapter.makeFuture(() -> {
            Matrix.getLogger().debug("Requesting player by name " + name);
            return cacheProvider.getPlayerByName(name).orElseGet(() -> {
                Matrix.getLogger().debug("Player not found on cache, falling back to storage " + name);
                return storage.getPlayerByName(name);
            });
        });
    }

    @Override
    public CompletableFuture<MatrixPlayer> getPlayerById(@NotNull String hexId) {
        return schedulerAdapter.makeFuture(() -> cacheProvider.getPlayerById(hexId).orElse(storage.getPlayerById(hexId)));
    }

    @Override
    public CompletableFuture<Boolean> isRegistered(@NotNull UUID uniqueId) {
        return schedulerAdapter.makeFuture(() -> {
            boolean cached = cacheProvider.isCached(uniqueId);
            if (cached) {
                return true;
            }
            return storage.getPlayer(uniqueId).isRegistered();
        });
    }

    @Override
    public CompletableFuture<Boolean> isRegisteredByName(@NotNull String name) {
        return schedulerAdapter.makeFuture(() -> {
            boolean cached = cacheProvider.isCachedByName(name);
            if (cached) {
                return true;
            }
            return storage.getPlayerByName(name).isRegistered();
        });
    }

    @Override
    public CompletableFuture<Boolean> isRegisteredById(@NotNull String hexId) {
        return schedulerAdapter.makeFuture(() -> {
            boolean cached = cacheProvider.isCachedById(hexId);
            if (cached) {
                return true;
            }
            return storage.getPlayerById(hexId).isRegistered();
        });
    }

    @Override
    public CompletableFuture<Void> purgeForAllPlayers(@NotNull String field) {
        return schedulerAdapter.makeFuture(() -> storage.purgeForAllPlayers(field));
    }

    @Override
    public CompletableFuture<Void> addFailedLogin(UUID uniqueId, String server, String message) {
        return schedulerAdapter.makeFuture(() -> storage.addFailedLogin(uniqueId, server, message));
    }

    @Override
    public CompletableFuture<Void> incrStatById(String hexId, String groupName, Statistic statistic, long value) {
        return schedulerAdapter.makeFuture(() -> storage.incrStatById(hexId, groupName, statistic, value));
    }

    @Override
    public CompletableFuture<Void> incrStatsById(String hexId, String groupName, Map<Statistic, Long> stats) {
        return schedulerAdapter.makeFuture(() -> {
            if (stats.isEmpty()) {
                return;
            }
            storage.incrStatsById(hexId, groupName, stats);
        });
    }


    @Override
    public CompletableFuture<Void> insertCommandLogEntryById(String hexId, String server, String command) {
        return schedulerAdapter.makeFuture(() -> storage.insertCommandLogEntryById(hexId, server, command));
    }

    @Override
    public CompletableFuture<Long> getStatById(String hexId, String groupName, Statistic statistic) {
        return schedulerAdapter.makeFuture(() -> storage.getStatById(hexId, groupName, statistic));
    }

    @Override
    public CompletableFuture<Long> getStatWeeklyById(String hexId, String groupName, Statistic statistic) {
        return schedulerAdapter.makeFuture(() -> storage.getStatWeeklyById(hexId, groupName, statistic));
    }

    @Override
    public CompletableFuture<Long> getStatMonthlyById(String hexId, String groupName, Statistic statistic) {
        return schedulerAdapter.makeFuture(() -> storage.getStatMonthlyById(hexId, groupName, statistic));
    }

    @Override
    public CompletableFuture<TopEntry[]> getTopStatTotal(String groupName, Statistic statistic) {
        return schedulerAdapter.makeFuture(() -> storage.getTopStatTotal(groupName, statistic));
    }

    @Override
    public CompletableFuture<TopEntry[]> getTopStatWeekly(String groupName, Statistic statistic) {
        return schedulerAdapter.makeFuture(() -> storage.getTopStatWeekly(groupName, statistic));
    }

    @Override
    public CompletableFuture<TopEntry[]> getTopStatMonthly(String groupName, Statistic statistic) {
        return schedulerAdapter.makeFuture(() -> storage.getTopStatMonthly(groupName, statistic));
    }

    @Override
    public CompletableFuture<Void> insertPlayStatsById(String hexId, String groupName, long playTime) {
        return schedulerAdapter.makeFuture(() -> storage.insertPlayStatsById(hexId, groupName, playTime));
    }

    @Override
    public CompletableFuture<PlayStats> getPlayStatsById(String hexId, String groupName) {
        return schedulerAdapter.makeFuture(() -> storage.getPlayStatsById(hexId, groupName));
    }

    @Override
    public <T> CompletableFuture<T> updateFieldById(String hexId, String s, T t) {
        return schedulerAdapter.makeFuture(() -> cacheProvider.updateCachedFieldById(hexId, s, t));
    }

    @Override
    public <T extends MatrixPlayer> CompletableFuture<Boolean> save(UUID uniqueId, T mongoMatrixPlayer) {
        return schedulerAdapter.makeFuture(() -> {
            try {
                cacheProvider.update(mongoMatrixPlayer.getName(), mongoMatrixPlayer.getUniqueId(), mongoMatrixPlayer.getId());
                cacheProvider.saveToCache(mongoMatrixPlayer);
                storage.save(cacheProvider.getPlayer(uniqueId).orElse(mongoMatrixPlayer));
            } catch (Exception e) {
                Matrix.getLogger().debug(e);
                return false;
            }
            return true;
        });
    }

    @Override
    public void cleanUp(MatrixPlayer matrixPlayer) {
        schedulerAdapter.makeFuture(() -> cacheProvider.removePlayer(matrixPlayer));
    }

    @Override
    public void shutdown() {
        cacheProvider.shutdown();
    }

    @Override
    public CacheProvider getCacheProvider() {
        return cacheProvider;
    }

    public StorageImpl getStorage() {
        return storage;
    }
}
