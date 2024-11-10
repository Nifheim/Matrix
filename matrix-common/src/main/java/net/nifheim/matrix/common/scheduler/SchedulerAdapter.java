package net.nifheim.matrix.common.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public interface SchedulerAdapter {


    @NotNull Executor async();

    @NotNull Executor sync();

    @NotNull SchedulerTask asyncLater(Runnable task, long delay, TimeUnit unit);

    @NotNull SchedulerTask asyncRepeating(Runnable task, long interval, TimeUnit unit);

    void executeAsync(Runnable runnable);

    void shutdownScheduler();

    void shutdownExecutor();

    <T> @NotNull CompletableFuture<T> makeFuture(@NotNull Callable<T> supplier);

    @NotNull CompletableFuture<Void> makeFuture(Throwing.Runnable runnable);
}
