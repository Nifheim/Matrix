package com.github.beelzebu.matrix.util;

import com.github.beelzebu.matrix.api.util.Throwing;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public final class Iterators {

    private Iterators() {
    }

    public static <E> boolean tryIterate(@NotNull Iterable<E> iterable, Throwing.@NotNull Consumer<E> action) {
        boolean success = true;
        for (E element : iterable) {
            try {
                action.accept(element);
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
        }
        return success;
    }

    public static <I, O> boolean tryIterate(@NotNull Iterable<I> iterable, @NotNull Function<I, O> mapping, @NotNull Consumer<O> action) {
        boolean success = true;
        for (I element : iterable) {
            try {
                action.accept(mapping.apply(element));
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
        }
        return success;
    }

    public static <E> boolean tryIterate(E @NotNull [] array, @NotNull Consumer<E> action) {
        boolean success = true;
        for (E element : array) {
            try {
                action.accept(element);
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
        }
        return success;
    }

    public static <I, O> boolean tryIterate(I @NotNull [] array, @NotNull Function<I, O> mapping, @NotNull Consumer<O> action) {
        boolean success = true;
        for (I element : array) {
            try {
                action.accept(mapping.apply(element));
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
            }
        }
        return success;
    }

    public static <E> @NotNull List<List<E>> divideIterable(@NotNull Iterable<E> source, int size) {
        List<List<E>> lists = new ArrayList<>();
        Iterator<E> it = source.iterator();
        while (it.hasNext()) {
            List<E> subList = new ArrayList<>();
            for (int i = 0; it.hasNext() && i < size; i++) {
                subList.add(it.next());
            }
            lists.add(subList);
        }
        return lists;
    }
}
