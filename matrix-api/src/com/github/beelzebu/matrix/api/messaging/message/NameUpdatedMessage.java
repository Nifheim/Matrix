package com.github.beelzebu.matrix.api.messaging.message;

import java.util.UUID;

/**
 * @author Beelzebu
 */
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

    public String getName() {
        return name;
    }

    public String getOldName() {
        return oldName;
    }

    public UUID getPlayerUniqueId() {
        return playerUniqueId;
    }

    public UUID getPlayerOldUniqueId() {
        return playerOldUniqueId;
    }

    @Override
    public void read() {
        // NOOP
    }

    @Override
    protected boolean onlyExternal() {
        return false;
    }

}
