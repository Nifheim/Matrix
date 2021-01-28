package com.github.beelzebu.matrix.util;

import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author Beelzebu
 */
public class SingleCachedValue <T> {

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(2);
    private WeakReference<T> value;
    private final Supplier<T> supplier;

    public SingleCachedValue(Supplier<T> supplier, long cacheTime, TimeUnit timeUnit) {
        this.supplier = supplier;
        value = new WeakReference<>(supplier.get());
        if (get().join() != null) {
            EXECUTOR.scheduleAtFixedRate(this::refresh, 0, cacheTime, timeUnit);
        }
    }

    public CompletableFuture<T> get() {
        T value = this.value.get();
        if (value != null) {
            return CompletableFuture.completedFuture(value);
        } else {
            return refresh();
        }
    }

    public CompletableFuture<T> refresh() {
        return CompletableFuture.supplyAsync(supplier, EXECUTOR).thenApply(val -> {
            this.value = new WeakReference<>(val);
            return val;
        });
    }
}
