package io.github.beelzebu.matrix.database.mongo;

import io.github.beelzebu.matrix.api.report.Report;
import io.github.beelzebu.matrix.api.report.ReportManager;
import io.github.beelzebu.matrix.report.MongoReport;
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
    public Optional<Report> getReport(int id) {
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
            save(report);
        }
        return findOne(createQuery().order(Sort.descending("_id")));
    }
}
