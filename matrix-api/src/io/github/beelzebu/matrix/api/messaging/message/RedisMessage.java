package io.github.beelzebu.matrix.api.messaging.message;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import java.util.UUID;

/**
 * @author Beelzebu
 */
public abstract class RedisMessage {

    protected transient final MatrixAPI api = Matrix.getAPI();
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
        if (onlyExternal()) {
            Matrix.getLogger().debug(getChannel() + " is only external");
            return;
        }
        Matrix.getLogger().debug("Reading " + getChannel() + " after sent");
        read();
    }

    /**
     * Get the unique id of this message, if the id is null a new one will be generated, so it will never be null.
     *
     * @return unique id of this message.
     */
    public final UUID getUniqueId() {
        return uniqueId == null ? uniqueId = UUID.randomUUID() : uniqueId;
    }

    protected abstract boolean onlyExternal();

    public abstract String getChannel();

    public abstract void read();
}
