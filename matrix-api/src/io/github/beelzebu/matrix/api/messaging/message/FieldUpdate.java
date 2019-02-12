package io.github.beelzebu.matrix.api.messaging.message;

import io.github.beelzebu.matrix.api.Matrix;
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
    public String getChannel() {
        return "api-field-update";
    }

    @Override
    public void read() {
        if (api.getPlugin().isOnline(getPlayer(), true)) {
            api.getPlayer(getPlayer()).setField(getField(), Matrix.GSON.fromJson(getJsonValue(), Object.class));
        }
    }

    @Override
    protected boolean onlyExternal() {
        return true;
    }
}
