package io.github.beelzebu.matrix.api.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ServerInfo {

    private final String serverName;
    private ServerType serverType;
}
