package io.github.beelzebu.matrix.database.mongo;

import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import io.github.beelzebu.matrix.player.MongoMatrixPlayer;
import java.util.Optional;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.dao.BasicDAO;

/**
 * @author Beelzebu
 */
public class UserDAO extends BasicDAO<MongoMatrixPlayer, ObjectId> {

    public UserDAO(Class<MongoMatrixPlayer> mpClass, Datastore ds) {
        super(mpClass, ds);
    }

    public MatrixPlayer getPlayer(UUID uniqueId) {
        return findOne("uniqueId", uniqueId);
    }

    public MatrixPlayer getPlayer(String name) {
        return Optional.ofNullable(findOne("name", name)).orElse(findOne("lowercaseName", name.toLowerCase()));
    }
}
