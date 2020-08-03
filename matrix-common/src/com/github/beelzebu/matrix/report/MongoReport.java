package com.github.beelzebu.matrix.report;

import cl.indiopikaro.jmatrix.api.report.Report;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

/**
 * @author Beelzebu
 */
@Entity(value = "reports", noClassnameStored = true)
public class MongoReport implements Report {

    @Id
    private long id = -1;
    @Reference(lazy = true, idOnly = true)
    private MongoMatrixPlayer reporter;
    @Reference(lazy = true, idOnly = true)
    private MongoMatrixPlayer reported;
    private String reason;

    public MongoReport() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public MongoMatrixPlayer getReporter() {
        return reporter;
    }

    public void setReporter(MongoMatrixPlayer reporter) {
        this.reporter = reporter;
    }

    public MongoMatrixPlayer getReported() {
        return reported;
    }

    public void setReported(MongoMatrixPlayer reported) {
        this.reported = reported;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String toString() {
        return "MongoReport(id=" + id + ", reporter=" + reporter + ", reported=" + reported + ", reason=" + reason + ")";
    }
}