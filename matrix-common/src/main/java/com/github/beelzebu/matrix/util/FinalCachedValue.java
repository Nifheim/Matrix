package com.github.beelzebu.matrix.util;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class FinalCachedValue <T> extends SingleCachedValue<T> {

    private final T value;

    public FinalCachedValue(@NotNull Supplier<T> supplier) {
        super(supplier, 0, null);
        value = supplier.get();
    }

    @Override
    public @NotNull CompletableFuture<T> get() {
        return CompletableFuture.completedFuture(value);
    }

    @Override
    public @NotNull CompletableFuture<T> refresh() {
        return get();
    }
}
