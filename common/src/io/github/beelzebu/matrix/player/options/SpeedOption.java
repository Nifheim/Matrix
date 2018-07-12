package io.github.beelzebu.matrix.player.options;

import io.github.beelzebu.matrix.player.MatrixPlayer;

public class SpeedOption extends Option {

    public SpeedOption(MatrixPlayer player) {
        super(player, player.getOption(PlayerOptionType.SPEED));
    }
}
