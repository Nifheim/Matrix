package io.github.beelzebu.matrix.api.messaging.message;

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
    public String getChannel() {
        return "api-message";
    }
}
