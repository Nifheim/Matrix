package com.github.beelzebu.matrix.api.database;

import com.github.beelzebu.matrix.api.player.Statistic;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * @author Beelzebu
 */
public interface SQLDatabase {

    void addFailedLogin(UUID uuid, String message);

    Future<Double> incrStat(UUID uuid, String server, String stat, double value);

    Future<Double> incrStat(UUID uuid, String server, Statistic stat, double value);

}
