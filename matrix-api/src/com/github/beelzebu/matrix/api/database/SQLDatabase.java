package com.github.beelzebu.matrix.api.database;

import com.github.beelzebu.matrix.api.player.Statistic;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * @author Beelzebu
 */
public interface SQLDatabase {

    void addFailedLogin(UUID uuid, String server, String message);

    // TODO: add double stat type
    Future<Void> incrStat(UUID uuid, String server, Statistic stat, long value);

    Future<Void> incrStats(UUID uuid, String server, Map<Statistic, Long> stats);

    Future<Long> getStat(UUID uuid, String server, Statistic statistic);
}
