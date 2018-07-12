package io.github.beelzebu.matrix.player.options;

import io.github.beelzebu.matrix.player.MatrixPlayer;

public class NickNameOption extends Option {

    public NickNameOption(MatrixPlayer player) {
        super(player, player.getOption(PlayerOptionType.NICKNAME));
    }
}
