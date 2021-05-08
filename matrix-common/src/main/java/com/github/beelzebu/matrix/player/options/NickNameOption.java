package com.github.beelzebu.matrix.player.options;

import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayerOptionType;
import org.jetbrains.annotations.NotNull;

public class NickNameOption extends Option {

    public NickNameOption(@NotNull MatrixPlayer player) {
        super(player, player.getOption(PlayerOptionType.NICKNAME));
    }
}
