package com.github.beelzebu.matrix.task;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Beelzebu
 */
public class ServerCleanupTask implements Runnable {

    public static long SLOW_HEARTBEAT = TimeUnit.MINUTES.toMillis(3);
    public static long NORMAL_HEARTBEAT = TimeUnit.MINUTES.toMillis(2);
    public static long DEAD_HEARTBEAT = TimeUnit.MINUTES.toMillis(5);
    private final MatrixAPI api;
    private final Map<String, Long> slowHeartbeats = new HashMap<>();

    public ServerCleanupTask(MatrixAPI api) {
        this.api = api;
    }

    @Override
    public void run() {
        api.getServerManager().getAllServers().thenAccept(map -> map.values().forEach(serverInfos -> serverInfos.forEach(serverInfo -> {
            long heartbeat = api.getServerManager().getLastHeartbeat(serverInfo).join().orElse(0);
            if (slowHeartbeats.containsKey(serverInfo.getServerName())) {
                if (slowHeartbeats.get(serverInfo.getServerName()) <= System.currentTimeMillis() - DEAD_HEARTBEAT) {
                    slowHeartbeats.remove(serverInfo.getServerName());
                    api.getServerManager().removeServer(serverInfo);
                    Matrix.getLogger().info("Removed " + serverInfo.getServerName() + " since heartbeat is dead");
                } else {
                    if (heartbeat < System.currentTimeMillis() - NORMAL_HEARTBEAT) {
                        slowHeartbeats.remove(serverInfo.getServerName());
                        Matrix.getLogger().info(serverInfo.getServerName() + " has a normal heartbeat, removed from slow heartbeats");
                    }
                }
            }
            if (heartbeat < System.currentTimeMillis() - SLOW_HEARTBEAT) {
                slowHeartbeats.put(serverInfo.getServerName(), heartbeat);
                Matrix.getLogger().info(serverInfo.getServerName() + " has a slow heartbeat");
            }
        })));
    }
}
