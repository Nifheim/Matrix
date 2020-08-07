package com.github.beelzebu.matrix.database.mongo;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.report.Report;
import com.github.beelzebu.matrix.api.report.ReportManager;
import com.github.beelzebu.matrix.report.MongoReport;
import java.util.Optional;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Sort;

/**
 * @author Beelzebu
 */
public class ReportDAO extends BasicDAO<MongoReport, Long> implements ReportManager {

    public ReportDAO(Datastore ds) {
        super(MongoReport.class, ds);
    }

    @Override
    public Optional<Report> getReport(long id) {
        return Optional.ofNullable(findOne("id", id));
    }

    @Override
    public Report createReport(Report report) throws IllegalArgumentException {
        return saveReport((MongoReport) report);
    }

    private Report saveReport(MongoReport report) {
        if (report.getId() != -1) {
            throw new IllegalArgumentException("Id in " + report.toString() + " isn't -1");
        } else {
            report.setId(((ReportDAO) Matrix.getAPI().getDatabase().getReportManager()).count() + 1);
            save(report);
        }
        return findOne(createQuery().order(Sort.descending("_id")));
    }
}
