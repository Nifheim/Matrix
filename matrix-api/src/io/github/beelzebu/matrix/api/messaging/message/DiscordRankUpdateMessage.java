package io.github.beelzebu.matrix.api.messaging.message;

import java.util.UUID;

/**
 * @author Beelzebu
 */
public class DiscordRankUpdateMessage extends RedisMessage {

    private final UUID userUniqueId;
    private final DiscordRankType rankType;

    public DiscordRankUpdateMessage(UUID userUniqueId, DiscordRankType rankType) {
        super(RedisMessageType.DISCORD_RANK_UPDATE);
        this.userUniqueId = userUniqueId;
        this.rankType = rankType;
    }

    @Override
    protected boolean onlyExternal() {
        return false;
    }

    @Override
    public void read() {
        // NOOP, must be handled by Hoenheim
    }

    public enum DiscordRankType {
        VERIFIED,
        VIP,
        NONE
    }
}
