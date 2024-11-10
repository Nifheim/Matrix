package net.nifheim.matrix.api.player.gamemode;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Enum for representing minecraft gamemodes.
 *
 * @author Jaime Suárez
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

    public static @Nullable GameMode getById(int id) {
        for (GameMode gameMode : values()) {
            if (gameMode.id == id) {
                return gameMode;
            }
        }
        return null;
    }

    @ApiStatus.Internal
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
