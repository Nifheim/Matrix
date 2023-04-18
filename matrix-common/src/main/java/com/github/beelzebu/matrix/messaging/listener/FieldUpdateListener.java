package com.github.beelzebu.matrix.messaging.listener;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.MessageListener;
import com.github.beelzebu.matrix.api.messaging.message.Message;
import com.github.beelzebu.matrix.api.messaging.message.MessageType;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.database.MatrixDatabaseImpl;
import com.github.beelzebu.matrix.messaging.message.FieldUpdateMessage;
import com.github.beelzebu.matrix.player.MongoMatrixPlayer;
import java.util.Objects;

/**
 * @author Jaime Suárez
 */
public class FieldUpdateListener extends MessageListener {

    private final MatrixDatabaseImpl database;

    public FieldUpdateListener(MatrixDatabaseImpl database) {
        super(MessageType.FIELD_UPDATE);
        this.database = database;

    }

    @Override
    public void onMessage(Message message) {
        Objects.requireNonNull(message.getContent(), "content");
        String hexId = FieldUpdateMessage.getPlayerId(message);
        Matrix.getLogger().debug("Received field update message for " + hexId);
        MatrixPlayer matrixPlayer = database.getCacheProvider().getLocalCached(hexId);
        if (matrixPlayer == null) {
            Matrix.getLogger().debug(hexId + " is not cached here, skipping message.");
            return;
        }
        String field = FieldUpdateMessage.getField(message);
        Matrix.getLogger().debug(hexId + " is " + matrixPlayer.getName() + "(" + matrixPlayer.getUniqueId() + ") updating field " + field + " with value: " + Matrix.GSON.toJson(message.getContent().get("value")));
        matrixPlayer.setField(field, FieldUpdateMessage.getValue(message, MongoMatrixPlayer.FIELDS.get(field).getType()));
    }
}
