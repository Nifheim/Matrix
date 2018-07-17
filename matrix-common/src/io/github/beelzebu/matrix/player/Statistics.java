package io.github.beelzebu.matrix.player;

import io.github.beelzebu.matrix.api.player.IStatistics;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

@Getter
@Setter
@AllArgsConstructor
@Entity(value = "statistics", noClassnameStored = false)
public class Statistics implements IStatistics {

    @Id
    private ObjectId id;
    private String server;
    private int playerKills;
    private int mobKills;
    private int deaths;
    private int blocksBroken;
    private int blocksPlaced;
}
