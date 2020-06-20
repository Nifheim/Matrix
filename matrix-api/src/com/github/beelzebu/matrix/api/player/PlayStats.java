package com.github.beelzebu.matrix.api.player;

import com.github.beelzebu.matrix.api.server.GameType;

/**
 * @author Beelzebu
 */
public class PlayStats {

    private final long joins;
    private final long playTime;
    private final GameType gameType;

    public PlayStats(long joins, long playTime, GameType gameType) {
        this.joins = joins;
        this.playTime = playTime;
        this.gameType = gameType;
    }

    public long getJoins() {
        return joins;
    }

    public long getPlayTime() {
        return playTime;
    }

    public GameType getGameType() {
        return gameType;
    }
}
