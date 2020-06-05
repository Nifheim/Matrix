package com.github.beelzebu.matrix.api.messaging.message;

/**
 * @author Beelzebu
 */
public class ServerRequestMessage extends RedisMessage {

    public ServerRequestMessage() {
        super(RedisMessageType.SERVER_REQUEST);
    }

    @Override
    protected boolean onlyExternal() {
        return false;
    }

    @Override
    public void read() {
        //NOOP, handled by bukkit
    }
}
