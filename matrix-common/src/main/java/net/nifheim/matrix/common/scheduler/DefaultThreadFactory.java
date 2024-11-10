package net.nifheim.matrix.common.scheduler;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

public class DefaultThreadFactory implements ThreadFactory {

    private final String name;
    private final boolean daemon;
    private final AtomicInteger threadId = new AtomicInteger(0);

    public DefaultThreadFactory(String name, boolean daemon) {
        this.name = name;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(@NotNull Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(daemon);
        thread.setName("%s-%d".formatted(name, threadId.getAndIncrement()));
        return thread;
    }
}
