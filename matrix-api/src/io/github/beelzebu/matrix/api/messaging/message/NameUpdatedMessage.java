package io.github.beelzebu.matrix.api.messaging.message;

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

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof io.github.beelzebu.matrix.api.messaging.message.NameUpdatedMessage)) {
            return false;
        }
        io.github.beelzebu.matrix.api.messaging.message.NameUpdatedMessage other = (io.github.beelzebu.matrix.api.messaging.message.NameUpdatedMessage) o;
        if (!other.canEqual((java.lang.Object) this)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        java.lang.Object this$name = getName();
        java.lang.Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) {
            return false;
        }
        java.lang.Object this$oldName = getOldName();
        java.lang.Object other$oldName = other.getOldName();
        if (this$oldName == null ? other$oldName != null : !this$oldName.equals(other$oldName)) {
            return false;
        }
        java.lang.Object this$playerUniqueId = getPlayerUniqueId();
        java.lang.Object other$playerUniqueId = other.getPlayerUniqueId();
        if (this$playerUniqueId == null ? other$playerUniqueId != null : !this$playerUniqueId.equals(other$playerUniqueId)) {
            return false;
        }
        java.lang.Object this$playerOldUniqueId = getPlayerOldUniqueId();
        java.lang.Object other$playerOldUniqueId = other.getPlayerOldUniqueId();
        if (this$playerOldUniqueId == null ? other$playerOldUniqueId != null : !this$playerOldUniqueId.equals(other$playerOldUniqueId)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        java.lang.Object $name = getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        java.lang.Object $oldName = getOldName();
        result = result * PRIME + ($oldName == null ? 43 : $oldName.hashCode());
        java.lang.Object $playerUniqueId = getPlayerUniqueId();
        result = result * PRIME + ($playerUniqueId == null ? 43 : $playerUniqueId.hashCode());
        java.lang.Object $playerOldUniqueId = getPlayerOldUniqueId();
        result = result * PRIME + ($playerOldUniqueId == null ? 43 : $playerOldUniqueId.hashCode());
        return result;
    }

    @Override
    public void read() {
        // NOOP
    }

    @Override
    protected boolean onlyExternal() {
        return false;
    }

    protected boolean canEqual(Object other) {
        return other instanceof io.github.beelzebu.matrix.api.messaging.message.NameUpdatedMessage;
    }
}
