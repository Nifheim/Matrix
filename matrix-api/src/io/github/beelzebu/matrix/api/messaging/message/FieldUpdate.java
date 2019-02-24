package io.github.beelzebu.matrix.api.messaging.message;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Beelzebu
 */
@Getter
@EqualsAndHashCode(callSuper = true)
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

    @Override
    protected boolean onlyExternal() {
        return true;
    }

    @Override
    public void read() {
        if (api.getPlugin().isOnline(getPlayer(), true)) {
            api.getPlayer(getPlayer()).setField(getField(), getJsonValue());
        }
    }
}
