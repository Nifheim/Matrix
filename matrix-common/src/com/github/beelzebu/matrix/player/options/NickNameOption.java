package com.github.beelzebu.matrix.player.options;

import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;
import cl.indiopikaro.jmatrix.api.player.PlayerOptionType;

public class NickNameOption extends Option {

    public NickNameOption(MatrixPlayer player) {
        super(player, player.getOption(PlayerOptionType.NICKNAME));
    }
}
