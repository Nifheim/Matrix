package com.github.beelzebu.matrix.player.options;

import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayerOptionType;

public class FlyOption extends Option {

    public FlyOption(MatrixPlayer player) {
        super(player, player.getOption(PlayerOptionType.FLY));
    }
}
