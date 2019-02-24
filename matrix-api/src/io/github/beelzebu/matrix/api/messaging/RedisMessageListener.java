package io.github.beelzebu.matrix.api.messaging;

import io.github.beelzebu.matrix.api.messaging.message.RedisMessage;
import io.github.beelzebu.matrix.api.messaging.message.RedisMessageType;

/**
 * @author Beelzebu
 */
public interface RedisMessageListener <T extends RedisMessage> {

    default void $$onMessage0$$(RedisMessage message) {
        if (message.getType() != getType()) {
            return;
        }
        onMessage((T) message);
    }

    void onMessage(T message);

    RedisMessageType getType();
}
