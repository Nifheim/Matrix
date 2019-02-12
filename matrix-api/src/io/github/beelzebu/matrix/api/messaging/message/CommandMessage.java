package io.github.beelzebu.matrix.api.messaging.message;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Beelzebu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommandMessage extends RedisMessage {

    private final String server;
    private final String command;
    private final boolean global;
    private final boolean bukkit;
    private final boolean bungee;

    @Override
    public String getChannel() {
        return "api-command";
    }

    @Override
    public void read() {
        if (isGlobal()) {
            api.getPlugin().executeCommand(getCommand());
        } else if (isBungee() && api.isBungee()) {
            api.getPlugin().executeCommand(getCommand());
        } else if (isBukkit() && !api.isBungee()) {
            api.getPlugin().executeCommand(getCommand());
        }
    }

    @Override
    protected boolean onlyExternal() {
        return false;
    }
}
