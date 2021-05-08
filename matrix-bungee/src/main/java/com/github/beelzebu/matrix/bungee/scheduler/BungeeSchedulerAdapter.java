package com.github.beelzebu.matrix.bungee.scheduler;

import com.github.beelzebu.matrix.api.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.api.scheduler.SchedulerTask;
import com.github.beelzebu.matrix.scheduler.AbstractJavaScheduler;
import com.github.beelzebu.matrix.util.Iterators;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class BungeeSchedulerAdapter extends AbstractJavaScheduler {

    private final @NotNull MatrixBungeeBootstrap bootstrap;

    private final @NotNull Executor executor;
    private final Set<ScheduledTask> tasks = Collections.newSetFromMap(new WeakHashMap<>());

    public BungeeSchedulerAdapter(@NotNull MatrixBungeeBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        this.executor = r -> bootstrap.getProxy().getScheduler().runAsync(bootstrap, r);
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
        ScheduledTask t = this.bootstrap.getProxy().getScheduler().schedule(this.bootstrap, task, delay, unit);
        this.tasks.add(t);
        return t::cancel;
    }

    @Override
    public @NotNull SchedulerTask asyncRepeating(Runnable task, long interval, TimeUnit unit) {
        ScheduledTask t = this.bootstrap.getProxy().getScheduler().schedule(this.bootstrap, task, 0, interval, unit);
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
