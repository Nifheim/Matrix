package io.github.beelzebu.matrix.api.messaging.message;

import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Beelzebu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FieldUpdate extends RedisMessage {

    private final UUID player;
    private final String field;
    private final String jsonValue;

    @Override
    protected boolean onlyExternal() {
        return true;
    }

    @Override
    public String getChannel() {
        return "api-field-update";
    }

    @Override
    public void read() {
        if (api.getPlugin().isOnline(getPlayer(), true)) {
            api.getPlayer(getPlayer()).setField(getField(), getJsonValue());
        }
    }
}
