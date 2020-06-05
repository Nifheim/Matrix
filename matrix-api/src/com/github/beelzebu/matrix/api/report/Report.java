package com.github.beelzebu.matrix.api.report;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;

/**
 * @author Beelzebu
 */
public interface Report {

    default Report create() {
        return Matrix.getAPI().getDatabase().getReportManager().createReport(this);
    }

    long getId();

    MatrixPlayer getReporter();

    MatrixPlayer getReported();

    String getReason();
}
