package com.github.beelzebu.matrix.util.bungee;

public class BungeeServerInfo {

    private volatile boolean isOnline;
    private volatile int onlinePlayers;
    private volatile int maxPlayers;
    private volatile long lastRequest;

    protected BungeeServerInfo() {
        isOnline = false;
        updateLastRequest();
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public int getOnlinePlayers() {
        return onlinePlayers;
    }

    public void setOnlinePlayers(int onlinePlayers) {
        this.onlinePlayers = onlinePlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public long getLastRequest() {
        return lastRequest;
    }

    public final void updateLastRequest() {
        lastRequest = System.currentTimeMillis();
    }
}
