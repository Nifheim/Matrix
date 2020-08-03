package com.github.beelzebu.matrix.scheduler;

import com.github.beelzebu.matrix.MatrixBukkitBootstrap;
import cl.indiopikaro.jmatrix.api.scheduler.SchedulerAdapter;
import java.util.concurrent.Executor;

/**
 * @author Beelzebu
 */
public class BukkitSchedulerAdapter extends AbstractJavaScheduler implements SchedulerAdapter {

    private final Executor sync;

    public BukkitSchedulerAdapter(MatrixBukkitBootstrap bootstrap) {
        this.sync = r -> bootstrap.getServer().getScheduler().scheduleSyncDelayedTask(bootstrap, r);
    }

    @Override
    public Executor sync() {
        return sync;
    }
}
