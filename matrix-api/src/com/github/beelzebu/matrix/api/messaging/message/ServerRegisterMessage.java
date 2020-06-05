package com.github.beelzebu.matrix.api.messaging.message;

import com.github.beelzebu.matrix.api.Matrix;

/**
 * @author Beelzebu
 */
public class ServerRegisterMessage extends RedisMessage {

    private final String group;
    private final String name;
    private final String ip;
    private final int port;

    public ServerRegisterMessage(String ip, int port) {
        super(RedisMessageType.SERVER_REGISTER);
        group = Matrix.getAPI().getServerInfo().getGroupName();
        name = Matrix.getAPI().getServerInfo().getServerName();
        this.ip = ip;
        this.port = port;
    }


    @Override
    protected boolean onlyExternal() {
        return false;
    }

    @Override
    public void read() {
        //NOOP, handled by bungeecord
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
