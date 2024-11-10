package net.nifheim.matrix.common.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * @author Jaime Suárez
 */
public abstract class AbstractJavaScheduler implements SchedulerAdapter {

    private final Logger logger;
    private final @NotNull ScheduledThreadPoolExecutor scheduler;
    private final @NotNull ErrorReportingExecutor schedulerWorkerPool;
    private final @NotNull ForkJoinPool worker;

    public AbstractJavaScheduler(Logger logger) {
        this.logger = logger;
        this.scheduler = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("matrix-scheduler", true));
        this.scheduler.setRemoveOnCancelPolicy(true);
        this.schedulerWorkerPool = new ErrorReportingExecutor(logger, Executors.newCachedThreadPool(new DefaultThreadFactory("matrix-scheduler-worker", true)));
        this.worker = new ForkJoinPool(32, ForkJoinPool.defaultForkJoinWorkerThreadFactory, (t, e) -> logger.error("Worker error", e), false);
    }

    @Override
    public @NotNull Executor async() {
        return this.worker;
    }

    @Override
    public @NotNull SchedulerTask asyncLater(Runnable task, long delay, TimeUnit unit) {
        ScheduledFuture<?> future = this.scheduler.schedule(() -> this.schedulerWorkerPool.execute(task), delay, unit);
        return () -> future.cancel(false);
    }

    @Override
    public @NotNull SchedulerTask asyncRepeating(Runnable task, long interval, TimeUnit unit) {
        ScheduledFuture<?> future = this.scheduler.scheduleAtFixedRate(() -> this.schedulerWorkerPool.execute(task), 0, interval, unit);
        return () -> future.cancel(false);
    }

    @Override
    public void executeAsync(Runnable runnable) {
        async().execute(runnable);
    }

    @Override
    public void shutdownScheduler() {
        this.scheduler.shutdown();
        try {
            this.scheduler.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error("Scheduler did not shutdown in a timely manner", e);
        }
    }

    @Override
    public void shutdownExecutor() {
        this.schedulerWorkerPool.delegate.shutdown();
        try {
            this.schedulerWorkerPool.delegate.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            logger.error("Scheduler worker pool did not shutdown in a timely manner", e);
        }
    }

    @Override
    public <T> @NotNull CompletableFuture<T> makeFuture(@NotNull Callable<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.call();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        }, async());
    }

    @Override
    public @NotNull CompletableFuture<Void> makeFuture(Throwing.Runnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new CompletionException(e);
            }
        }, async());
    }

    private record ErrorReportingExecutor(Logger logger, ExecutorService delegate) implements Executor {

        @Override
        public void execute(@NotNull Runnable command) {
            this.delegate.execute(new ErrorReportingRunnable(logger, command));
        }
    }

    private record ErrorReportingRunnable(Logger logger, Runnable delegate) implements Runnable {

        @Override
        public void run() {
            try {
                this.delegate.run();
            } catch (Exception e) {
                logger.error("Error in task", e);
            }
        }
    }
}
