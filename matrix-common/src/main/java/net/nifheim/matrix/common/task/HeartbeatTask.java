package net.nifheim.matrix.common.task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.nifheim.matrix.api.server.ServerManager;
import org.slf4j.Logger;

/**
 * @author Jaime Suárez
 */
public class HeartbeatTask implements Runnable {

    private final ServerManager serverManager;
    private final Logger logger;

    public HeartbeatTask(ServerManager serverManager, Logger logger) {
        this.serverManager = serverManager;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            serverManager.heartbeat().get(5, TimeUnit.SECONDS);
        } catch (RuntimeException | InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error occurred while sending heartbeat", e);
        }
    }
}
