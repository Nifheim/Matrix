package io.github.beelzebu.matrix.database;

import io.github.beelzebu.matrix.MatrixAPI;
import io.github.beelzebu.matrix.player.MatrixPlayer;
import io.github.beelzebu.matrix.player.Statistics;

public class MySQLStorage {

    private final MatrixAPI core;

    public MySQLStorage(MatrixAPI core) {
        this.core = core;
    }

    public void saveStats(MatrixPlayer player, Statistics stats) {

    }
}
