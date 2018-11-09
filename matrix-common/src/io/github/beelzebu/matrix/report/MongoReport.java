package io.github.beelzebu.matrix.report;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.report.Report;
import io.github.beelzebu.matrix.database.mongo.ReportDAO;
import io.github.beelzebu.matrix.player.MongoMatrixPlayer;
import lombok.Data;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

/**
 * @author Beelzebu
 */
@Data
@Entity(value = "reports", noClassnameStored = true)
public class MongoReport implements Report {

    @Id
    private final long id = ((ReportDAO) Matrix.getAPI().getDatabase().getReportManager()).count() + 1;
    @Reference(lazy = true, idOnly = true)
    private MongoMatrixPlayer reporter;
    @Reference(lazy = true, idOnly = true)
    private MongoMatrixPlayer reported;
    private String reason;
}