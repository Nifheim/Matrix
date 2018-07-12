package io.github.beelzebu.matrix.player.options;

import io.github.beelzebu.matrix.player.MatrixPlayer;

public class FlyOption extends Option {

    public FlyOption(MatrixPlayer player) {
        super(player, player.getOption(PlayerOptionType.FLY));
    }
}
