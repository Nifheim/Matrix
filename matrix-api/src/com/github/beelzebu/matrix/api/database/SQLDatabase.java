package com.github.beelzebu.matrix.api.database;

import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayStats;
import com.github.beelzebu.matrix.api.player.Statistic;
import com.github.beelzebu.matrix.api.server.GameType;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * @author Beelzebu
 */
public interface SQLDatabase {

    void addFailedLogin(UUID uniqueId, String server, String message);

    // TODO: add double stat type
    Future<Void> incrStat(MatrixPlayer matrixPlayer, String server, Statistic stat, long value);

    @Deprecated
    Future<Void> incrStat(UUID uniqueId, String server, Statistic stat, long value);

    Future<Void> incrStats(MatrixPlayer matrixPlayer, String server, Map<Statistic, Long> stats);

    @Deprecated
    Future<Void> incrStats(UUID uniqueId, String server, Map<Statistic, Long> stats);

    Future<Void> insertCommandLogEntry(MatrixPlayer matrixPlayer, String server, String command);

    @Deprecated
    Future<Void> insertCommandLogEntry(UUID uniqueId, String server, String command);

    Future<Long> getStat(MatrixPlayer matrixPlayer, String server, Statistic statistic);

    @Deprecated
    Future<Long> getStat(UUID uniqueId, String server, Statistic statistic);

    Future<Void> insertPlayStats(MatrixPlayer matrixPlayer, GameType gameType, long playTime);

    Future<PlayStats> getPlayStats(MatrixPlayer matrixPlayer, GameType gameType);
}
