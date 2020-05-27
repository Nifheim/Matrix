package io.github.beelzebu.matrix.api.messaging.message;

import java.util.UUID;

/**
 * @author Beelzebu
 */
public class FieldUpdate extends RedisMessage {

    private final UUID player;
    private final String field;
    private final String jsonValue;

    public FieldUpdate(UUID player, String field, String jsonValue) {
        super(RedisMessageType.FIELD_UPDATE);
        this.player = player;
        this.field = field;
        this.jsonValue = jsonValue;
    }

    public UUID getPlayer() {
        return player;
    }

    public String getField() {
        return field;
    }

    public String getJsonValue() {
        return jsonValue;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof io.github.beelzebu.matrix.api.messaging.message.FieldUpdate)) {
            return false;
        }
        io.github.beelzebu.matrix.api.messaging.message.FieldUpdate other = (io.github.beelzebu.matrix.api.messaging.message.FieldUpdate) o;
        if (!other.canEqual((java.lang.Object) this)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        java.lang.Object this$player = getPlayer();
        java.lang.Object other$player = other.getPlayer();
        if (this$player == null ? other$player != null : !this$player.equals(other$player)) {
            return false;
        }
        java.lang.Object this$field = getField();
        java.lang.Object other$field = other.getField();
        if (this$field == null ? other$field != null : !this$field.equals(other$field)) {
            return false;
        }
        java.lang.Object this$jsonValue = getJsonValue();
        java.lang.Object other$jsonValue = other.getJsonValue();
        if (this$jsonValue == null ? other$jsonValue != null : !this$jsonValue.equals(other$jsonValue)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        java.lang.Object $player = getPlayer();
        result = result * PRIME + ($player == null ? 43 : $player.hashCode());
        java.lang.Object $field = getField();
        result = result * PRIME + ($field == null ? 43 : $field.hashCode());
        java.lang.Object $jsonValue = getJsonValue();
        result = result * PRIME + ($jsonValue == null ? 43 : $jsonValue.hashCode());
        return result;
    }

    @Override
    public void read() {
        if (api.getPlugin().isOnline(getPlayer(), true)) {
            api.getPlayer(getPlayer()).setField(getField(), getJsonValue());
        }
    }

    @Override
    protected boolean onlyExternal() {
        return true;
    }

    protected boolean canEqual(Object other) {
        return other instanceof io.github.beelzebu.matrix.api.messaging.message.FieldUpdate;
    }
}
