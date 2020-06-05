package com.github.beelzebu.matrix.api.messaging;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.message.RedisMessage;
import com.github.beelzebu.matrix.api.messaging.message.RedisMessageType;
import java.util.Objects;

/**
 * @author Beelzebu
 */
public interface RedisMessageListener <T extends RedisMessage> {

    default void $$onMessage0$$(RedisMessage message) {
        Objects.requireNonNull(message, "Can't pass a null message");
        if (!Objects.equals(message.getType(), getType())) {
            return;
        }
        Matrix.getLogger().debug("Reading message on: " + this.getClass().getSimpleName());
        onMessage((T) message);
    }

    void onMessage(T message);

    RedisMessageType getType();
}
