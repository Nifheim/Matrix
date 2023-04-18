package com.github.beelzebu.matrix.bukkit.scheduler;

import com.github.beelzebu.matrix.api.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.scheduler.SchedulerAdapter;
import com.github.beelzebu.matrix.scheduler.AbstractJavaScheduler;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jaime Suárez
 */
public class BukkitSchedulerAdapter extends AbstractJavaScheduler implements SchedulerAdapter {

    private final @NotNull Executor sync;

    public BukkitSchedulerAdapter(@NotNull MatrixBukkitBootstrap bootstrap) {
        this.sync = r -> bootstrap.getServer().getScheduler().scheduleSyncDelayedTask(bootstrap, r);
    }

    @Override
    public @NotNull Executor sync() {
        return sync;
    }
}
