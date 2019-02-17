package io.github.beelzebu.matrix.api.messaging.message;

import io.github.beelzebu.coins.api.utils.StringUtils;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Beelzebu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TargetedMessage extends RedisMessage {

    private final UUID target;
    private final String message;

    @Override
    protected boolean onlyExternal() {
        return false;
    }

    @Override
    public String getChannel() {
        return "api-message";
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
