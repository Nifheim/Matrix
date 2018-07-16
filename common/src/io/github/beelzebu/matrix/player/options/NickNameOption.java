package io.github.beelzebu.matrix.player.options;

import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.player.PlayerOptionType;

public class NickNameOption extends Option {

    public NickNameOption(MatrixPlayer player) {
        super(player, player.getOption(PlayerOptionType.NICKNAME));
    }
}
