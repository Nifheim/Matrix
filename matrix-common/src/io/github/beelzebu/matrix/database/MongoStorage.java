package io.github.beelzebu.matrix.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.database.MatrixDatabase;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.api.report.ReportManager;
import io.github.beelzebu.matrix.database.mongo.ReportDAO;
import io.github.beelzebu.matrix.database.mongo.UserDAO;
import io.github.beelzebu.matrix.player.MongoMatrixPlayer;
import io.github.beelzebu.matrix.player.Statistics;
import io.github.beelzebu.matrix.report.MongoReport;
import java.util.UUID;
import lombok.Getter;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.converters.UUIDConverter;

/**
 * @author Beelzebu
 */
@Getter
public class MongoStorage implements MatrixDatabase {

    private final MongoClient client;
    private final Morphia morphia;
    private final Datastore datastore;
    private final UserDAO userDAO;
    private final ReportDAO reportDAO;

    public MongoStorage(String host, int port, String username, String database, String password, String databaselogin) {
        client = new MongoClient(new ServerAddress(host, port), MongoCredential.createCredential(username, databaselogin, password.toCharArray()), MongoClientOptions.builder().build());
        morphia = new Morphia();
        morphia.map(MongoMatrixPlayer.class, Statistics.class, MongoReport.class);
        morphia.getMapper().getConverters().addConverter(new UUIDConverter());
        datastore = morphia.createDatastore(client, database);
        datastore.ensureIndexes();
        userDAO = new UserDAO(datastore);
        reportDAO = new ReportDAO(datastore);
    }

    @Override
    public MatrixPlayer getPlayer(UUID uniqueId) {
        return userDAO.getPlayer(uniqueId);
    }

    @Override
    public MatrixPlayer getPlayer(String name) {
        return userDAO.getPlayer(name);
    }

    @Override
    public boolean isRegistered(UUID uniqueId) {
        if (Matrix.getAPISafe().isPresent()) {
            return Matrix.getAPI().getPlayer(uniqueId) != null;
        }
        return userDAO.getPlayer(uniqueId) != null;
    }

    @Override
    public boolean isRegistered(String name) {
        if (Matrix.getAPISafe().isPresent()) {
            return Matrix.getAPI().getPlayer(name) != null;
        }
        return userDAO.getPlayer(name) != null;
    }

    @Override
    public ReportManager getReportManager() {
        return reportDAO;
    }
}
