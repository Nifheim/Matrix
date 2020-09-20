package com.github.beelzebu.matrix.util;

/**
 * @author Beelzebu
 */
public interface Throwing {

    @FunctionalInterface
    interface Runnable {

        void run() throws Exception;
    }

    @FunctionalInterface
    interface Consumer <T> {

        void accept(T t) throws Exception;
    }
}