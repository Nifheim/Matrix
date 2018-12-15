package io.github.beelzebu.matrix.api.messaging.message;

import java.util.UUID;
import lombok.Data;

/**
 * @author Beelzebu
 */
@Data
public class AuthMessage implements RedisMessage {

    private final UUID user;
    private final boolean authed;

    @Override
    public String getChannel() {
        return "api-auth";
    }
}
