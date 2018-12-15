package io.github.beelzebu.matrix.api.messaging.message;

import lombok.Data;

/**
 * @author Beelzebu
 */
@Data
public class CommandMessage implements RedisMessage {

    private final String server;
    private final String command;
    private final boolean global;
    private final boolean bukkit;
    private final boolean bungee;

    @Override
    public String getChannel() {
        return "api-command";
    }
}
