package io.github.beelzebu.matrix.api.messaging.message;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Beelzebu
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class NameUpdatedMessage extends RedisMessage {

    private final String name;
    private final String oldName;
    private final UUID playerUniqueId;
    private final UUID playerOldUniqueId;

    public NameUpdatedMessage(String name, String oldName, UUID playerUniqueId, UUID playerOldUniqueId) {
        super(RedisMessageType.NAME_UPDATE);
        this.name = name;
        this.oldName = oldName;
        this.playerUniqueId = playerUniqueId;
        this.playerOldUniqueId = playerOldUniqueId;
    }

    @Override
    protected boolean onlyExternal() {
        return false;
    }

    @Override
    public void read() {
        // NOOP
    }
}
