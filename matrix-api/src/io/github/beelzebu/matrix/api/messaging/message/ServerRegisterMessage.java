package io.github.beelzebu.matrix.api.messaging.message;

import io.github.beelzebu.matrix.api.Matrix;

/**
 * @author Beelzebu
 */
public class ServerRegisterMessage extends RedisMessage {

    private final String name;
    private final String ip;
    private final int port;

    public ServerRegisterMessage(String ip, int port) {
        super(RedisMessageType.SERVER_REGISTER);
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
