package com.github.beelzebu.matrix.database;

import cl.indiopikaro.jmatrix.api.Matrix;
import cl.indiopikaro.jmatrix.api.database.MatrixDatabase;
import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;
import cl.indiopikaro.jmatrix.api.report.ReportManager;
import com.github.beelzebu.matrix.database.mongo.ChatColorConverter;
import com.github.beelzebu.matrix.database.mongo.ReportDAO;
import com.github.beelzebu.matrix.database.mongo.UserDAO;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.converters.UUIDConverter;

/**
 * @author Beelzebu
 */
public class MongoStorage implements MatrixDatabase {

    private final UserDAO userDAO;
    private final ReportDAO reportDAO;

    public MongoStorage(String host, int port, String username, String database, String password, String databaselogin) {
        MongoClient client = new MongoClient(new ServerAddress(host, port), MongoCredential.createCredential(username, databaselogin, password.toCharArray()), MongoClientOptions.builder().build());
        Morphia morphia = new Morphia();
        morphia.map(MongoMatrixPlayer.class);
        morphia.getMapper().getConverters().addConverter(new UUIDConverter());
        morphia.getMapper().getConverters().addConverter(new ChatColorConverter());
        Datastore datastore = morphia.createDatastore(client, database);
        datastore.ensureIndexes();
        userDAO = new UserDAO(MongoMatrixPlayer.class, datastore);
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
    public MatrixPlayer getPlayerById(String hexId) {
        return userDAO.get(new ObjectId(hexId));
    }

    @Override
    public boolean isRegistered(UUID uniqueId) {
        return Matrix.getAPI().getCache().isCached(uniqueId) || userDAO.getPlayer(uniqueId) != null;
    }

    @Override
    public boolean isRegistered(String name) {
        return Matrix.getAPI().getCache().getPlayer(name).isPresent() || userDAO.getPlayer(name) != null;
    }

    @Override
    public void purgeForAllPlayers(String field) {
        userDAO.createUpdateOperations().unset(field);
    }

    @Override
    public ReportManager getReportManager() {
        return reportDAO;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public ReportDAO getReportDAO() {
        return reportDAO;
    }
}
