package net.nifheim.matrix.velocity.scheduler;

import com.velocitypowered.api.scheduler.ScheduledTask;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import net.nifheim.matrix.velocity.bootstrap.MatrixVelocityBootstrap;
import net.nifheim.matrix.common.scheduler.AbstractJavaScheduler;
import net.nifheim.matrix.common.scheduler.SchedulerTask;
import net.nifheim.matrix.common.util.Iterators;
import org.jetbrains.annotations.NotNull;

public class VelocitySchedulerAdapter extends AbstractJavaScheduler {

    private final @NotNull MatrixVelocityBootstrap bootstrap;

    private final @NotNull Executor executor;
    private final Set<ScheduledTask> tasks = Collections.newSetFromMap(new WeakHashMap<>());

    public VelocitySchedulerAdapter(@NotNull MatrixVelocityBootstrap bootstrap) {
        super(bootstrap.getPlatformLogger());
        this.bootstrap = bootstrap;
        this.executor = r -> bootstrap.getServer().getScheduler().buildTask(bootstrap, r).schedule();
    }

    @Override
    public @NotNull Executor async() {
        return this.executor;
    }

    @Override
    public @NotNull Executor sync() {
        return this.executor;
    }

    @Override
    public @NotNull SchedulerTask asyncLater(Runnable task, long delay, TimeUnit unit) {
        ScheduledTask t = this.bootstrap.getServer().getScheduler().buildTask(this.bootstrap, task).delay(delay, unit).schedule();
        this.tasks.add(t);
        return t::cancel;
    }

    @Override
    public @NotNull SchedulerTask asyncRepeating(Runnable task, long interval, TimeUnit unit) {
        ScheduledTask t = this.bootstrap.getServer().getScheduler().buildTask(this.bootstrap, task).repeat(interval, unit).schedule();
        this.tasks.add(t);
        return t::cancel;
    }

    @Override
    public void shutdownScheduler() {
        Iterators.tryIterate(this.tasks, ScheduledTask::cancel);
    }

    @Override
    public void shutdownExecutor() {
        // do nothing
    }
}
