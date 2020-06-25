package com.github.beelzebu.matrix.api.messaging.message;

import com.github.beelzebu.matrix.api.Matrix;
import java.util.Objects;

/**
 * @author Beelzebu
 */
public class ServerRegisterMessage extends RedisMessage {

    private final String group;
    private final String name;
    private final String ip;
    private final int port;

    public ServerRegisterMessage(String group, String name, String ip, int port) {
        super(RedisMessageType.SERVER_REGISTER);
        this.group = Objects.requireNonNull(group, "group can't be null.");
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public ServerRegisterMessage(String ip, int port) {
        this(Matrix.getAPI().getServerInfo().getGroupName(), Matrix.getAPI().getServerInfo().getServerName(), ip, port);
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
