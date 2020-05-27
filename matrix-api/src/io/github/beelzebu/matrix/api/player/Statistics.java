package io.github.beelzebu.matrix.api.player;

public class Statistics {

    private String server;
    private int playerKills;
    private int mobKills;
    private int deaths;
    private int blocksBroken;
    private int blocksPlaced;

    public Statistics(String server, int playerKills, int mobKills, int deaths, int blocksBroken, int blocksPlaced) {
        this.server = server;
        this.playerKills = playerKills;
        this.mobKills = mobKills;
        this.deaths = deaths;
        this.blocksBroken = blocksBroken;
        this.blocksPlaced = blocksPlaced;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPlayerKills() {
        return playerKills;
    }

    public void setPlayerKills(int playerKills) {
        this.playerKills = playerKills;
    }

    public int getMobKills() {
        return mobKills;
    }

    public void setMobKills(int mobKills) {
        this.mobKills = mobKills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getBlocksBroken() {
        return blocksBroken;
    }

    public void setBlocksBroken(int blocksBroken) {
        this.blocksBroken = blocksBroken;
    }

    public int getBlocksPlaced() {
        return blocksPlaced;
    }

    public void setBlocksPlaced(int blocksPlaced) {
        this.blocksPlaced = blocksPlaced;
    }
}
