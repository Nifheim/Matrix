package io.github.beelzebu.matrix.api.messaging;

/**
 * @author Beelzebu
 */
public interface RedisMessageEvent {

    void onMessage(String channel, String message);
}
