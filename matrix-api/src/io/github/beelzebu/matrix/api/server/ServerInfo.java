package io.github.beelzebu.matrix.api.server;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerInfo {

    private final String serverName;
    private ServerType serverType;

    public ServerInfo(String name) {
        serverName = name;
    }
}
