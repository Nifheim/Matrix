package io.github.beelzebu.matrix.api.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Statistics {

    private String server;
    private int playerKills;
    private int mobKills;
    private int deaths;
    private int blocksBroken;
    private int blocksPlaced;
}
