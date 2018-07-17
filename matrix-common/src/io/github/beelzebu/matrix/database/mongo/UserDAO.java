package io.github.beelzebu.matrix.database.mongo;

import io.github.beelzebu.matrix.player.MongoMatrixPlayer;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * @author Beelzebu
 */
public class UserDAO extends BasicDAO<MongoMatrixPlayer, ObjectId> {

    public UserDAO(Datastore ds) {
        super(MongoMatrixPlayer.class, ds);
    }

    public MongoMatrixPlayer getPlayer(UUID uniqueId) {
        return findOne("uuid", uniqueId);
    }

    public MongoMatrixPlayer getPlayer(String name) {
        return findOne("name", name);
    }
}
