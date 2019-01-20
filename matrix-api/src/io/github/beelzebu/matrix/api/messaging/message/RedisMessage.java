package io.github.beelzebu.matrix.api.messaging.message;

import io.github.beelzebu.matrix.api.Matrix;
import java.util.UUID;

/**
 * @author Beelzebu
 */
public abstract class RedisMessage {

    private UUID uniqueId;
    private String channel;

    /**
     * Send this message though redis to notify other servers.
     */
    public final void send() {
        if (channel == null) {
            channel = getChannel();
        }
        Matrix.getAPI().getRedis().sendMessage(this);
    }

    /**
     * Get the unique id of this message, if the id is null a new one will be generated, so it will never be null.
     *
     * @return unique id of this message.
     */
    public final UUID getUniqueId() {
        return uniqueId == null ? uniqueId = UUID.randomUUID() : uniqueId;
    }

    public String getChannel() {
        return getClass().getName();
    }
}
