package com.github.beelzebu.matrix.task;

import com.github.beelzebu.matrix.api.MatrixAPI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Beelzebu
 */
public class ServerCleanupTask implements Runnable {

    public static long SLOW_HEARTBEAT = TimeUnit.MINUTES.toMillis(10);
    public static long NORMAL_HEARTBEAT = TimeUnit.MINUTES.toMillis(5);
    public static long DEAD_HEARTBEAT = TimeUnit.MINUTES.toMillis(15);
    private final MatrixAPI api;
    private final Map<String, Long> slowHeartbeats = new HashMap<>();

    public ServerCleanupTask(MatrixAPI api) {
        this.api = api;
    }

    @Override
    public void run() {
        api.getCache().getAllServers().values().forEach(serverInfos -> serverInfos.forEach(serverInfo -> {
            long heartbeat = api.getCache().getLastHeartbeat(serverInfo);
            if (slowHeartbeats.containsKey(serverInfo.getServerName())) {
                if (slowHeartbeats.get(serverInfo.getServerName()) <= System.currentTimeMillis() - DEAD_HEARTBEAT) {
                    slowHeartbeats.remove(serverInfo.getServerName());
                    api.getCache().removeServer(serverInfo);
                } else {
                    if (heartbeat < System.currentTimeMillis() - NORMAL_HEARTBEAT) {
                        slowHeartbeats.remove(serverInfo.getServerName());
                    }
                }
            }
            if (heartbeat < System.currentTimeMillis() - SLOW_HEARTBEAT) {
                slowHeartbeats.put(serverInfo.getServerName(), heartbeat);
            }
        }));
    }
}