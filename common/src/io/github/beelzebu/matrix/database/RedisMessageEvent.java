package io.github.beelzebu.matrix.database;

/**
 * @author Beelzebu
 */
public interface RedisMessageEvent {

    public void onMessage(String channel, String message);

}
