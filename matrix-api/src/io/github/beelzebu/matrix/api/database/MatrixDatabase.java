package io.github.beelzebu.matrix.api.database;

import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.report.ReportManager;
import java.util.UUID;

/**
 * @author Beelzebu
 */
public interface MatrixDatabase {

    MatrixPlayer getPlayer(UUID uniqueId);

    MatrixPlayer getPlayer(String name);

    boolean isRegistered(UUID uniqueId);

    boolean isRegistered(String name);

    ReportManager getReportManager();
}
