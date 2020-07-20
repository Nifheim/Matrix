package com.github.beelzebu.matrix.api.messaging;

import com.github.beelzebu.matrix.api.messaging.message.RedisMessage;

/**
 * @author Beelzebu
 */
public interface Messaging {

    /**
     * Send message using redis pub/sub, the message will be converted to JSON and converted to objects again in other
     * servers if they should manage it. Also it will save the unique id of this message so it won't be handled by the
     * listener of this instance.
     *
     * @param redisMessage message to send though redis.
     */
    void sendMessage(RedisMessage redisMessage);

    void registerListener(RedisMessageListener<? extends RedisMessage> redisMessageListener);

    void shutdown();
}
