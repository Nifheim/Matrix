package io.github.beelzebu.matrix.api.messaging.message;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Beelzebu
 */
@Getter
@EqualsAndHashCode(callSuper = true)
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
