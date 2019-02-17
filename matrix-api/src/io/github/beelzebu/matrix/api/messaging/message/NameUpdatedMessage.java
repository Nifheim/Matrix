package io.github.beelzebu.matrix.api.messaging.message;

import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Beelzebu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NameUpdatedMessage extends RedisMessage {

    private final String name;
    private final String oldName;
    private final UUID playerUniqueId;
    private final UUID playerOldUniqueId;

    @Override
    protected boolean onlyExternal() {
        return false;
    }

    @Override
    public String getChannel() {
        return "name-updated";
    }

    @Override
    public void read() {
        // NOOP
    }
}
