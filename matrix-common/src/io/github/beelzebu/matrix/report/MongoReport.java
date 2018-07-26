package io.github.beelzebu.matrix.report;

import io.github.beelzebu.matrix.api.report.Report;
import io.github.beelzebu.matrix.player.MongoMatrixPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

/**
 * @author Beelzebu
 */
@Setter
@Getter
@ToString
@RequiredArgsConstructor
@Entity(value = "reports", noClassnameStored = true)
public class MongoReport implements Report {

    @Id
    private int id = 0;
    @Reference(lazy = true, idOnly = true)
    private MongoMatrixPlayer reporter;
    @Reference(lazy = true, idOnly = true)
    private MongoMatrixPlayer reported;
    private String reason;
}