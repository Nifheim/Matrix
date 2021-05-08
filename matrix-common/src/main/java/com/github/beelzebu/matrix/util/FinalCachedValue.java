package com.github.beelzebu.matrix.util;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * @author Beelzebu
 */
public class FinalCachedValue <T> extends SingleCachedValue<T> {

    private final T value;

    public FinalCachedValue(Supplier<T> supplier) {
        super(supplier, 0, null);
        value = supplier.get();
    }

    @Override
    public CompletableFuture<T> get() {
        return CompletableFuture.completedFuture(value);
    }

    @Override
    public CompletableFuture<T> refresh() {
        return get();
    }
}
