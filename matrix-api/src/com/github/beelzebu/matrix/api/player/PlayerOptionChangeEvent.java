package com.github.beelzebu.matrix.api.player;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Beelzebu
 */
public class PlayerOptionChangeEvent {

    public static final Set<PlayerOptionChangeListener> LISTENERS = new HashSet<>();
    private final MatrixPlayer matrixPlayer;
    private final PlayerOptionType optionType;
    private final boolean oldState;
    private final boolean state;

    public PlayerOptionChangeEvent(MatrixPlayer matrixPlayer, PlayerOptionType optionType, boolean oldState, boolean state) {
        this.matrixPlayer = matrixPlayer;
        this.optionType = optionType;
        this.oldState = oldState;
        this.state = state;
    }

    public MatrixPlayer getMatrixPlayer() {
        return matrixPlayer;
    }

    public PlayerOptionType getOptionType() {
        return optionType;
    }

    public boolean getOldState() {
        return oldState;
    }

    public boolean getState() {
        return state;
    }

    public static abstract class PlayerOptionChangeListener {

        public PlayerOptionChangeListener() {
            LISTENERS.add(this);
        }

        public abstract void onPlayerOptionChange(PlayerOptionChangeEvent e);
    }
}
