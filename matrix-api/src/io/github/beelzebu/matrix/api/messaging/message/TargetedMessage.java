package io.github.beelzebu.matrix.api.messaging.message;

import io.github.beelzebu.coins.api.utils.StringUtils;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Beelzebu
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class TargetedMessage extends RedisMessage {

    private final UUID target;
    private final String message;

    public TargetedMessage(UUID target, String message) {
        super(RedisMessageType.TARGETED_MESSAGE);
        this.target = target;
        this.message = message;
    }

    @Override
    protected boolean onlyExternal() {
        return false;
    }

    @Override
    public void read() {
        if (api.isBungee()) {
            if (api.getPlugin().isOnline(getTarget(), true)) {
                api.getPlugin().sendMessage(getTarget(), StringUtils.rep(getMessage()));
            }
        }
    }
}
