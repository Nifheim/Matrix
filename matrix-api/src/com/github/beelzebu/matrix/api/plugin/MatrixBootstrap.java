package com.github.beelzebu.matrix.api.plugin;

import com.github.beelzebu.matrix.api.scheduler.SchedulerAdapter;

/**
 * @author Beelzebu
 */
public interface MatrixBootstrap {

    SchedulerAdapter getScheduler();

    /**
     * Get the matrixPlugin instance, it can be null for implementations that don't run on a minecraft server.
     *
     * @return matrixPlugin instance if exists, null otherwise.
     */
    MatrixPlugin getMatrixPlugin();
}
