package io.github.beelzebu.matrix.player.options;

import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.player.PlayerOptionType;

public class SpeedOption extends Option {

    public SpeedOption(MatrixPlayer player) {
        super(player, player.getOption(PlayerOptionType.SPEED));
    }
}
