package com.github.beelzebu.matrix.bungee.util;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ProxyServer;

/**
 * @author Jaime Suárez
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
        api.getServerManager().getAllServers().thenAccept(map -> {
            Iterator<Set<ServerInfo>> groups = map.values().iterator();
            while (groups.hasNext()) {
                Set<ServerInfo> group = groups.next();
                Iterator<ServerInfo> serverInfoIterator = group.iterator();
                while (serverInfoIterator.hasNext()) {
                    ServerInfo serverInfo = serverInfoIterator.next();
                    long heartbeat = api.getServerManager().getLastHeartbeat(serverInfo).join().orElse(0);
                    if (slowHeartbeats.containsKey(serverInfo.getServerName())) {
                        if (slowHeartbeats.get(serverInfo.getServerName()) <= System.currentTimeMillis() - DEAD_HEARTBEAT) {
                            slowHeartbeats.remove(serverInfo.getServerName());
                            api.getServerManager().removeServer(serverInfo);
                            ProxyServer.getInstance().getServers().remove(serverInfo.getServerName());
                            Matrix.getLogger().info("Removed " + serverInfo.getServerName() + " since heartbeat is dead");
                            serverInfoIterator.remove();
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
                }
                if (group.isEmpty()) {
                    groups.remove();
                }
            }
        });
    }
}
