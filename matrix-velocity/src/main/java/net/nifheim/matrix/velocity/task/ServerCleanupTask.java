package net.nifheim.matrix.velocity.task;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import net.nifheim.matrix.api.server.ServerInfo;
import net.nifheim.matrix.common.server.ServerManagerImpl;
import org.slf4j.Logger;

/**
 * @author Jaime Suárez
 */
public class ServerCleanupTask implements Runnable {

    public static long SLOW_HEARTBEAT = TimeUnit.MINUTES.toMillis(3);
    public static long NORMAL_HEARTBEAT = TimeUnit.MINUTES.toMillis(2);
    public static long DEAD_HEARTBEAT = TimeUnit.MINUTES.toMillis(5);
    private final Logger logger;
    private final ServerManagerImpl serverManager;
    private final ProxyServer server;
    private final Map<String, Long> slowHeartbeats = new HashMap<>();

    public ServerCleanupTask(Logger logger, ServerManagerImpl serverManager, ProxyServer server) {
        this.logger = logger;
        this.serverManager = serverManager;
        this.server = server;
    }


    @Override
    public void run() {
        Map<String, Set<ServerInfo>> map = serverManager.getAllServersSync();
        Iterator<Set<ServerInfo>> groups = map.values().iterator();
        while (groups.hasNext()) {
            Set<ServerInfo> group = groups.next();
            Iterator<ServerInfo> serverInfoIterator = group.iterator();
            while (serverInfoIterator.hasNext()) {
                ServerInfo serverInfo = serverInfoIterator.next();
                OptionalLong lastHeartbeat = serverManager.getLastHeartbeatSync(serverInfo);
                if (lastHeartbeat.isEmpty()) {
                    logger.warn("No heartbeat received from {}", serverInfo.getName());
                    continue;
                }
                long heartbeat = lastHeartbeat.getAsLong();
                double elapsedSeconds = (System.currentTimeMillis() - heartbeat) / 1000.0;
                if (slowHeartbeats.containsKey(serverInfo.getName())) {
                    if (slowHeartbeats.get(serverInfo.getName()) <= System.currentTimeMillis() - DEAD_HEARTBEAT) {
                        slowHeartbeats.remove(serverInfo.getName());
                        serverManager.removeServer(serverInfo);
                        server.getServer(serverInfo.getName()).map(RegisteredServer::getServerInfo).ifPresent(server::unregisterServer);
                        logger.warn("Removed {} since heartbeat is dead, elapsed time: {} seconds", serverInfo.getName(), elapsedSeconds);
                        serverInfoIterator.remove();
                    } else {
                        if (heartbeat < System.currentTimeMillis() - NORMAL_HEARTBEAT) {
                            slowHeartbeats.remove(serverInfo.getName());
                            logger.info("{} has a normal heartbeat again, removed from slow heartbeats, elapsed time: {} seconds", serverInfo.getName(), elapsedSeconds);
                        }
                    }
                } else if (heartbeat < System.currentTimeMillis() - SLOW_HEARTBEAT) {
                    slowHeartbeats.put(serverInfo.getName(), heartbeat);
                    logger.warn("{} has a slow heartbeat, elapsed time: {} seconds", serverInfo.getName(), elapsedSeconds);
                }
            }
            if (group.isEmpty()) {
                groups.remove();
            }
        }
    }
}
