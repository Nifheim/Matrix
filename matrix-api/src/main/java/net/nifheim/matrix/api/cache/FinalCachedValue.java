package net.nifheim.matrix.api.cache;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class representing a cached value that cannot be modified once initialized.
 *
 * @param <T> The type of the cached value
 * @author Jaime Suárez
 */
public class FinalCachedValue <T> implements SingleCachedValue<T> {

    private final T value;

    public FinalCachedValue(@NotNull Supplier<T> supplier) {
        value = supplier.get();
    }

    @Override
    public @NotNull CompletableFuture<T> get() {
        return CompletableFuture.completedFuture(value);
    }

    @Override
    public @Nullable T getLastValue() {
        return value;
    }
}
