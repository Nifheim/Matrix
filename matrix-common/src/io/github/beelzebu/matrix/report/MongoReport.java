package io.github.beelzebu.matrix.report;

import io.github.beelzebu.matrix.api.report.Report;
import io.github.beelzebu.matrix.player.MongoMatrixPlayer;
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

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof io.github.beelzebu.matrix.report.MongoReport)) {
            return false;
        }
        io.github.beelzebu.matrix.report.MongoReport other = (io.github.beelzebu.matrix.report.MongoReport) o;
        if (!other.canEqual((java.lang.Object) this)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        java.lang.Object this$reporter = reporter;
        java.lang.Object other$reporter = other.reporter;
        if (this$reporter == null ? other$reporter != null : !this$reporter.equals(other$reporter)) {
            return false;
        }
        java.lang.Object this$reported = reported;
        java.lang.Object other$reported = other.reported;
        if (this$reported == null ? other$reported != null : !this$reported.equals(other$reported)) {
            return false;
        }
        java.lang.Object this$reason = reason;
        java.lang.Object other$reason = other.reason;
        if (this$reason == null ? other$reason != null : !this$reason.equals(other$reason)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        long $id = id;
        result = result * PRIME + (int) ($id >>> 32 ^ $id);
        java.lang.Object $reporter = reporter;
        result = result * PRIME + ($reporter == null ? 43 : $reporter.hashCode());
        java.lang.Object $reported = reported;
        result = result * PRIME + ($reported == null ? 43 : $reported.hashCode());
        java.lang.Object $reason = reason;
        result = result * PRIME + ($reason == null ? 43 : $reason.hashCode());
        return result;
    }

    public String toString() {
        return "MongoReport(id=" + id + ", reporter=" + reporter + ", reported=" + reported + ", reason=" + reason + ")";
    }

    protected boolean canEqual(Object other) {
        return other instanceof io.github.beelzebu.matrix.report.MongoReport;
    }
}