package io.github.beelzebu.matrix.database;

import io.github.beelzebu.matrix.MatrixAPI;
import io.github.beelzebu.matrix.player.MatrixPlayer;
import io.github.beelzebu.matrix.player.Statistics;
import io.github.beelzebu.matrix.utils.ServerType;
import java.util.UUID;

/**
 * @author Beelzebu
 */
public class MongoStorage {

    public MongoStorage(MatrixAPI core) {
    }

    public void createPlayer(UUID uuid, String nick) {

    }

    public void saveStats(UUID uuid, String server, Statistics stats) {

    }

    public void saveStats(MatrixPlayer player, String server, ServerType serverType, Statistics stats) {

    }

    public void setData(UUID uuid, String property, Object value) {

    }

    public long getXP(UUID uuid) {
        return 0;
    }

    public boolean isRegistred(UUID uuid) {
        return false;
    }

    public boolean isRegistred(UUID uuid, String server) {
        return false;
    }

    public boolean isRegistred(String name) {
        return false;
    }

    public boolean isRegistred(String name, String server) {
        return false;
    }

    public String getName(UUID uuid) {
        return null;
    }

    public UUID getUUID(String name) {
        return null;
    }

    public void publish(String channel, String message) {

    }
}
