package com.github.beelzebu.matrix.api.messaging.message;

import com.github.beelzebu.matrix.api.Matrix;

/**
 * @author Beelzebu
 */
public class ServerUnregisterMessage extends RedisMessage {

    private final String name;

    public ServerUnregisterMessage() {
        super(RedisMessageType.SERVER_UNREGISTER);
        name = Matrix.getAPI().getServerInfo().getServerName();
    }

    public String getName() {
        return name;
    }

    @Override
    protected boolean onlyExternal() {
        return false;
    }

    @Override
    public void read() {
        //NOOP, handled by bungeecord
    }
}
