package io.github.beelzebu.matrix.api.report;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;

/**
 * @author Beelzebu
 */
public interface Report {

    long getId();

    MatrixPlayer getReporter();

    MatrixPlayer getReported();

    String getReason();

    default Report create() {
        return Matrix.getAPI().getDatabase().getReportManager().createReport(this);
    }
}
