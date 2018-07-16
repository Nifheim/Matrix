package io.github.beelzebu.matrix.api.messaging;

/**
 * @author Beelzebu
 */
public interface RedisMessaging {

    void sendMessage(String channel, String message);

}
