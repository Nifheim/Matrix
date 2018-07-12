package io.github.beelzebu.matrix.player;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Statistics {

    private final int playerKills;
    private final int mobKills;
    private final int deaths;
    private final int blocksBroken;
    private final int blocksPlaced;
    private final long lastlogin;
}
