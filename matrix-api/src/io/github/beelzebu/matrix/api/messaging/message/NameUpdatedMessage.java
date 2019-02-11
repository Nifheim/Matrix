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
    private final UUID uniqueId;
    private final UUID oldUniqueId;

    @Override
    public String getChannel() {
        return "name-updated";
    }
}
