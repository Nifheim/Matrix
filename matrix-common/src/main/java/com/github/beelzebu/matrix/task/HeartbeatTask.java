package com.github.beelzebu.matrix.task;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPIImpl;

/**
 * @author Beelzebu
 */
public class HeartbeatTask implements Runnable {

    private final MatrixAPIImpl<?> api;

    public HeartbeatTask(MatrixAPIImpl<?> api) {
        this.api = api;
    }

    @Override
    public void run() {
        try {
            api.getServerManager().heartbeat(api.getServerInfo()).join();
        } catch (RuntimeException e) {
            Matrix.getLogger().debug(e);
        }
    }
}
