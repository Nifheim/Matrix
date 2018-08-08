package io.github.beelzebu.matrix.utils.bungee;

import io.github.beelzebu.matrix.Main;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;

public class BungeeServerTracker {

    private static final Map<String, BungeeServerInfo> trackedServers = new ConcurrentHashMap<>();
    private static int taskID = -1;

    public static void resetTrackedServers() {
        trackedServers.clear();
    }

    public static void track(String server) {
        if (!trackedServers.containsKey(server)) {
            BungeeServerInfo info = new BungeeServerInfo();
            trackedServers.put(server, info);
            PluginMessage.get().askPlayerCount(server);
        }
    }

    public static void untrack(String server) {
        trackedServers.remove(server);
    }

    protected static BungeeServerInfo getOrCreateServerInfo(String server) {
        BungeeServerInfo info = trackedServers.get(server);
        if (info == null) {
            info = new BungeeServerInfo();
            trackedServers.put(server, info);
        }
        return info;
    }

    public static int getPlayersOnline(String server) {
        BungeeServerInfo info = trackedServers.get(server);
        if (info != null) {
            info.updateLastRequest();
            return info.getOnlinePlayers();
        } else {
            track(server);
            return 0;
        }
    }

    public static Map<String, BungeeServerInfo> getTrackedServers() {
        return trackedServers;
    }

    public static void startTask(int refreshSeconds) {
        if (taskID != -1) {
            Bukkit.getScheduler().cancelTask(taskID);
        }
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(Main.class), () -> {
            for (String server : trackedServers.keySet()) {
                PluginMessage.get().askPlayerCount(server);
            }
        }, 1, refreshSeconds * 20);
    }
}
