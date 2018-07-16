package io.github.beelzebu.matrix.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.database.MatrixDatabase;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.database.mongo.UserDAO;
import io.github.beelzebu.matrix.player.MongoMatrixPlayer;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

/**
 * @author Beelzebu
 */
@Getter
@Setter
public class MongoStorage implements MatrixDatabase {

    private final MongoClient client;
    private final Morphia morphia;
    private final Datastore datastore;
    private UserDAO userDAO;

    public MongoStorage(String host, int port, String username, String database, String password, String databaselogin) {
        client = new MongoClient(new ServerAddress(host, port), MongoCredential.createCredential(username, databaselogin, password.toCharArray()), MongoClientOptions.builder().build());
        morphia = new Morphia();
        morphia.map(MongoMatrixPlayer.class);
        datastore = morphia.createDatastore(client, database);
        datastore.ensureIndexes();
        setUserDAO(new UserDAO(datastore));
    }

    @Override
    public MatrixPlayer getPlayer(UUID uniqueId) {
        return null;
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
}
