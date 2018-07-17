package io.github.beelzebu.matrix.api.player;

/**
 * @author Beelzebu
 */
public interface IStatistics {

    String getServer();

    int getPlayerKills();

    int getMobKills();

    int getDeaths();

    int getBlocksBroken();

    int getBlocksPlaced();
}
