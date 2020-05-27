package io.github.beelzebu.matrix.api.messaging.message;

import io.github.beelzebu.matrix.api.util.StringUtils;
import java.util.UUID;

/**
 * @author Beelzebu
 */
public class TargetedMessage extends RedisMessage {

    private final UUID target;
    private final String message;

    public TargetedMessage(UUID target, String message) {
        super(RedisMessageType.TARGETED_MESSAGE);
        this.target = target;
        this.message = message;
    }

    public UUID getTarget() {
        return target;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void read() {
        if (api.isBungee()) {
            if (api.getPlugin().isOnline(getTarget(), true)) {
                api.getPlugin().sendMessage(getTarget(), StringUtils.replace(getMessage()));
            }
        }
    }

    @Override
    protected boolean onlyExternal() {
        return false;
    }

    protected boolean canEqual(Object other) {
        return other instanceof io.github.beelzebu.matrix.api.messaging.message.TargetedMessage;
    }
}
