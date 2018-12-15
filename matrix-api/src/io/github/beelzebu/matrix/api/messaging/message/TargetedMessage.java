package io.github.beelzebu.matrix.api.messaging.message;

import java.util.UUID;
import lombok.Data;

/**
 * @author Beelzebu
 */
@Data
public class TargetedMessage implements RedisMessage {

    private final UUID target;
    private final String message;

    @Override
    public String getChannel() {
        return "api-message";
    }
}
