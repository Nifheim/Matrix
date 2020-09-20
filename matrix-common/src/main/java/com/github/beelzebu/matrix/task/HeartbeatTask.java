package com.github.beelzebu.matrix.task;

import com.github.beelzebu.matrix.api.MatrixAPI;

/**
 * @author Beelzebu
 */
public class HeartbeatTask implements Runnable {

    private final MatrixAPI api;

    public HeartbeatTask(MatrixAPI api) {
        this.api = api;
    }

    @Override
    public void run() {
        api.getCache().heartbeat(api.getServerInfo());
    }
}