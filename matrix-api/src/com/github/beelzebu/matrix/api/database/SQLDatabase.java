package com.github.beelzebu.matrix.api.database;

import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayStats;
import com.github.beelzebu.matrix.api.player.Statistic;
import com.github.beelzebu.matrix.api.player.TopEntry;
import com.github.beelzebu.matrix.api.server.GameType;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Beelzebu
 */
public interface SQLDatabase {

    void addFailedLogin(UUID uniqueId, String server, String message);

    // TODO: add double stat type
    CompletableFuture<Void> incrStat(MatrixPlayer matrixPlayer, String server, Statistic stat, long value);

    @Deprecated
    CompletableFuture<Void> incrStat(UUID uniqueId, String server, Statistic stat, long value);

    CompletableFuture<Void> incrStats(MatrixPlayer matrixPlayer, String server, Map<Statistic, Long> stats);

    @Deprecated
    CompletableFuture<Void> incrStats(UUID uniqueId, String server, Map<Statistic, Long> stats);

    CompletableFuture<Void> insertCommandLogEntry(MatrixPlayer matrixPlayer, String server, String command);

    @Deprecated
    CompletableFuture<Void> insertCommandLogEntry(UUID uniqueId, String server, String command);

    CompletableFuture<Long> getStat(MatrixPlayer matrixPlayer, String server, Statistic statistic);

    @Deprecated
    CompletableFuture<Long> getStat(UUID uniqueId, String server, Statistic statistic);

    CompletableFuture<Long> getStatWeekly(MatrixPlayer matrixPlayer, String server, Statistic statistic);

    CompletableFuture<Long> getStatMonthly(MatrixPlayer matrixPlayer, String server, Statistic statistic);

    CompletableFuture<TopEntry[]> getTopStatTotal(String server, Statistic statistic);

    CompletableFuture<TopEntry[]> getTopStatWeekly(String server, Statistic statistic);

    CompletableFuture<TopEntry[]> getTopStatMonthly(String server, Statistic statistic);

    CompletableFuture<Void> insertPlayStats(MatrixPlayer matrixPlayer, GameType gameType, long playTime);

    CompletableFuture<PlayStats> getPlayStats(MatrixPlayer matrixPlayer, GameType gameType);
}
