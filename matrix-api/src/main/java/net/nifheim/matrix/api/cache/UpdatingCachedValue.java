package net.nifheim.matrix.api.cache;

import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * A class that represents an updating cached value. It implements the SingleCachedValue interface.
 * This class allows retrieving the value asynchronously using CompletableFuture and keeps track of the last value retrieved.
 *
 * @param <T> The type of the cached value
 * @author Jaime Suárez
 */
public class UpdatingCachedValue <T> implements SingleCachedValue<T> {

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(2);
    private WeakReference<T> value;
    private T lastValue;
    private final @NotNull Supplier<T> supplier;
    private final Lock lock = new ReentrantLock();

    public UpdatingCachedValue(@NotNull Supplier<T> supplier, long cacheTime, @NotNull TimeUnit timeUnit) {
        this.supplier = supplier;
        value = new WeakReference<>(supplier.get());
        if (get().join() != null) {
            EXECUTOR.scheduleAtFixedRate(this::refresh, 0, cacheTime, timeUnit);
        }
    }

    @Override
    public CompletableFuture<T> get() {
        T value = this.value.get();
        if (value != null) {
            return CompletableFuture.completedFuture(value);
        } else {
            return refresh();
        }
    }

    @Override

    public T getLastValue() {
        return lastValue;
    }

    public CompletableFuture<T> refresh() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                lock.lock();
                return supplier.get();
            } finally {
                lock.unlock();
            }
        }, EXECUTOR).thenApply(val -> {
            this.value = new WeakReference<>(val);
            this.lastValue = val;
            return val;
        });
    }
}
