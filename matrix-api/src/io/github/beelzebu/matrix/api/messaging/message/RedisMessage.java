package io.github.beelzebu.matrix.api.messaging.message;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import java.util.UUID;

/**
 * @author Beelzebu
 */
public abstract class RedisMessage {

    protected transient static final MatrixAPI api = Matrix.getAPI();
    private final RedisMessageType redisMessageType;
    private UUID uniqueId;

    public RedisMessage(RedisMessageType redisMessageType) {
        this.redisMessageType = redisMessageType;
    }

    /**
     * Send this message though redis to notify other servers.
     */
    public final void send() {
        Matrix.getAPI().getRedis().sendMessage(this);
        if (onlyExternal()) {
            Matrix.getLogger().debug(redisMessageType + " is only external");
            return;
        }
        Matrix.getLogger().debug("Reading " + redisMessageType + " after sent");
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

    public RedisMessageType getType() {
        return redisMessageType;
    }

    protected abstract boolean onlyExternal();

    public abstract void read();

    public static RedisMessage getFromType(RedisMessageType redisMessageType, String jsonMessage) {
        switch (redisMessageType) {
            case COMMAND:
                return Matrix.GSON.fromJson(jsonMessage, CommandMessage.class);
            case NAME_UPDATE:
                return Matrix.GSON.fromJson(jsonMessage, NameUpdatedMessage.class);
            case STAFF_CHAT:
                return Matrix.GSON.fromJson(jsonMessage, StaffChatMessage.class);
            case FIELD_UPDATE:
                return Matrix.GSON.fromJson(jsonMessage, FieldUpdate.class);
            case TARGETED_MESSAGE:
                return Matrix.GSON.fromJson(jsonMessage, TargetedMessage.class);
            case DISCORD_RANK_UPDATE:
                return Matrix.GSON.fromJson(jsonMessage, DiscordRankUpdateMessage.class);
            case SERVER_REGISTER:
                return Matrix.GSON.fromJson(jsonMessage, ServerRegisterMessage.class);
            default:
                return null;
        }
    }
}
