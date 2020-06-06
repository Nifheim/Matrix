package com.github.beelzebu.matrix.api.player;

public enum Statistic {

    KILLS(3, "kills"),
    MOB_KILLS(4, "mobKills"),
    DEATHS(5, "deaths"),
    BLOCKS_BROKEN(6, "blocksBroken"),
    BLOCKS_PLACED(7, "blocksPlaced");

    private final int id;
    private final String databaseName;

    Statistic(int id, String databaseName) {
        this.id = id;
        this.databaseName = databaseName;
    }

    public int getId() {
        return id;
    }

    public String getDatabaseName() {
        return databaseName;
    }
}
