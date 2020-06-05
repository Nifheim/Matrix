package com.github.beelzebu.matrix.api.messaging.message;

/**
 * @author Beelzebu
 */
public class CommandMessage extends RedisMessage {

    private final String server;
    private final String command;
    private final boolean global;
    private final boolean bungee;
    private final boolean bukkit;

    public CommandMessage(String server, String command, boolean global, boolean bungee, boolean bukkit) {
        super(RedisMessageType.COMMAND);
        this.server = server;
        this.command = command;
        this.global = global;
        this.bungee = bungee;
        this.bukkit = bukkit;
    }

    public String getServer() {
        return server;
    }

    public String getCommand() {
        return command;
    }

    public boolean isGlobal() {
        return global;
    }

    public boolean isBungee() {
        return bungee;
    }

    public boolean isBukkit() {
        return bukkit;
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

    protected boolean canEqual(Object other) {
        return other instanceof CommandMessage;
    }
}
