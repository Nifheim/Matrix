package io.github.beelzebu.matrix.api.messaging.message;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author Beelzebu
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class CommandMessage extends RedisMessage {

    private final String server;
    private final String command;
    private final boolean global;
    private final boolean bukkit;
    private final boolean bungee;

    public CommandMessage(String server, String command, boolean global, boolean bukkit, boolean bungee) {
        super(RedisMessageType.COMMAND);
        this.server = server;
        this.command = command;
        this.global = global;
        this.bukkit = bukkit;
        this.bungee = bungee;
    }

    @Override
    protected boolean onlyExternal() {
        return false;
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
}
