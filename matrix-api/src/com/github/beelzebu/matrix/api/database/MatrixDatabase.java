package com.github.beelzebu.matrix.api.database;

import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.report.ReportManager;
import java.util.UUID;

/**
 * @author Beelzebu
 */
public interface MatrixDatabase {

    MatrixPlayer getPlayer(UUID uniqueId);

    MatrixPlayer getPlayer(String name);

    MatrixPlayer getPlayerById(String hexId);

    boolean isRegistered(UUID uniqueId);

    boolean isRegistered(String name);

    void purgeForAllPlayers(String field);

    ReportManager getReportManager();
}
