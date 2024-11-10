package net.nifheim.matrix.paper.scheduler;

import java.util.concurrent.Executor;
import net.nifheim.matrix.paper.MatrixPaper;
import net.nifheim.matrix.common.scheduler.AbstractJavaScheduler;
import net.nifheim.matrix.common.scheduler.SchedulerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jaime Suárez
 */
public class PaperSchedulerAdapter extends AbstractJavaScheduler implements SchedulerAdapter {

    private final @NotNull Executor sync;

    public PaperSchedulerAdapter(@NotNull MatrixPaper bootstrap) {
        super(bootstrap.getPlatformLogger());
        this.sync = r -> bootstrap.getServer().getScheduler().scheduleSyncDelayedTask(bootstrap, r);
    }

    @Override
    public @NotNull Executor sync() {
        return sync;
    }
}
