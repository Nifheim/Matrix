package net.nifheim.matrix.common.messaging.listener;

import java.util.Objects;
import java.util.Optional;
import net.nifheim.matrix.api.messaging.MessageListener;
import net.nifheim.matrix.api.messaging.message.StandardChannel;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.player.PlayerManager;
import net.nifheim.matrix.common.messaging.message.FieldUpdateMessage;
import net.nifheim.matrix.common.player.MongoMatrixPlayer;
import net.nifheim.matrix.common.player.PlayerManagerImpl;
import org.slf4j.Logger;

/**
 * @author Jaime Suárez
 */
public class FieldUpdateListener extends MessageListener<FieldUpdateMessage> {

    private final PlayerManagerImpl<?> playerManager;
    private final Logger logger;

    public FieldUpdateListener(PlayerManagerImpl<?> playerManager, Logger logger) {
        super(StandardChannel.UPDATE_FIELD);
        this.playerManager = playerManager;
        this.logger = logger;
    }

    @Override
    public void onMessage(FieldUpdateMessage message) {
        Objects.requireNonNull(message.getContent(), "content");
        String hexId = message.getPlayerId();
        logger.debug("Received field update message for {}", hexId);
        Optional<? extends MatrixPlayer> localCachedPlayer = playerManager.getCacheProvider().getLocallyCached(hexId);
        if (localCachedPlayer.isEmpty()) {
            logger.debug("{} is not cached here, skipping message.", hexId);
            return;
        }
        MatrixPlayer matrixPlayer = localCachedPlayer.get();
        String fieldName = message.getField();
        String rawValue = message.getRawValue();
        logger.debug("{} is {} ({}) updating field {} with value: {}", hexId, matrixPlayer.getName(), matrixPlayer.getUniqueId(), fieldName, rawValue);
        try {
            // TODO: centralize this logic in a single place, fields read.
            playerManager.updateProperty(matrixPlayer, fieldName, message.getValue(PlayerManagerImpl.FIELDS.get(fieldName).getType()));
        } catch (ReflectiveOperationException e) {
            logger.error("Error updating field {} for player {}({}) with value {}", fieldName, matrixPlayer.getName(), matrixPlayer.getUniqueId(), rawValue, e);
        }
    }
}
