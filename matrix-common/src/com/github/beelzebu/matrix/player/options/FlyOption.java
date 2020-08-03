package com.github.beelzebu.matrix.player.options;

import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;
import cl.indiopikaro.jmatrix.api.player.PlayerOptionType;

public class FlyOption extends Option {

    public FlyOption(MatrixPlayer player) {
        super(player, player.getOption(PlayerOptionType.FLY));
    }
}
