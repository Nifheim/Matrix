package com.github.beelzebu.matrix.api.player;

/**
 * @author Beelzebu
 */
public enum GameMode {
    SURVIVAL(0),
    CREATIVE(1),
    ADVENTURE(2),
    SPECTATOR(3);

    private final int id;

    GameMode(int id) {
        this.id = id;
    }

    public static GameMode getById(int id) {
        for (GameMode gameMode : values()) {
            if (gameMode.id == id) {
                return gameMode;
            }
        }
        return null;
    }

    @Deprecated
    public int getId() {
        return id;
    }
}
