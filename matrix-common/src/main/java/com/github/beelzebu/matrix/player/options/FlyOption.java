package com.github.beelzebu.matrix.player.options;

import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.player.PlayerOptionType;
import org.jetbrains.annotations.NotNull;

public class FlyOption extends Option {

    public FlyOption(@NotNull MatrixPlayer player) {
        super(player, player.getOption(PlayerOptionType.FLY));
    }
}
