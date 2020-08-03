package com.github.beelzebu.matrix.player.options;

import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;
import cl.indiopikaro.jmatrix.api.player.PlayerOptionType;

public class SpeedOption extends Option {

    public SpeedOption(MatrixPlayer player) {
        super(player, player.getOption(PlayerOptionType.SPEED));
    }
}
