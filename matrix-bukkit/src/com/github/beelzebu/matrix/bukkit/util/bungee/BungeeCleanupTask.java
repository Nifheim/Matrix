package com.github.beelzebu.matrix.bukkit.util.bungee;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class BungeeCleanupTask implements Runnable {

    private static final long MAX_INACTIVITY = TimeUnit.MINUTES.toMillis(10);

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        Iterator<Entry<String, BungeeServerInfo>> iter = BungeeServerTracker.getTrackedServers().entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, BungeeServerInfo> next = iter.next();
            long lastRequest = next.getValue().getLastRequest();
            if (lastRequest != 0 && now - lastRequest > MAX_INACTIVITY) {
                iter.remove();
            }
        }
    }
}
