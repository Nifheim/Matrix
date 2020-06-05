package com.github.beelzebu.matrix.api.messaging.message;

import java.util.UUID;

/**
 * @author Beelzebu
 */
public class DiscordRankUpdateMessage extends RedisMessage {

    private final UUID userUniqueId;
    private final DiscordRankType rankType;
    private final Action action;

    public DiscordRankUpdateMessage(UUID userUniqueId, DiscordRankType rankType, Action action) {
        super(RedisMessageType.DISCORD_RANK_UPDATE);
        this.userUniqueId = userUniqueId;
        this.rankType = rankType;
        this.action = action;
    }

    public UUID getUserUniqueId() {
        return userUniqueId;
    }

    public DiscordRankType getRankType() {
        return rankType;
    }

    public Action getAction() {
        return action;
    }

    @Override
    public void read() {
        // NOOP, must be handled by Hoenheim
    }

    @Override
    protected boolean onlyExternal() {
        return false;
    }

    protected boolean canEqual(Object other) {
        return other instanceof DiscordRankUpdateMessage;
    }

    public enum DiscordRankType {
        VERIFIED,
        VIP,
        NONE
    }

    public enum Action {
        CHECK,
        REMOVE,
        ADD;

        public boolean isAdd() {
            return this == ADD;
        }

        public boolean isRemove() {
            return this == REMOVE;
        }

        public boolean isCheck() {
            return this == CHECK;
        }
    }
}
