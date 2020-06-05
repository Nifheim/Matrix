package com.github.beelzebu.matrix.api.messaging.message;

import com.github.beelzebu.matrix.api.Matrix;
import net.md_5.bungee.api.ChatColor;

/**
 * @author Beelzebu
 */
public class StaffChatMessage extends RedisMessage {

    private final String permission;
    private final String message;

    public StaffChatMessage(String permission, String message) {
        super(RedisMessageType.STAFF_CHAT);
        this.permission = permission;
        this.message = message;
    }

    public String getMessage() {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getPermission() {
        return permission;
    }

    @Override
    public void read() {
        if (api.isBungee()) {
            api.getPlayers().stream().filter(p -> api.hasPermission(p, getPermission())).forEach(p -> api.getPlugin().sendMessage(p.getUniqueId(), getMessage()));
            Matrix.getLogger().info(getMessage());
        }
    }

    @Override
    protected boolean onlyExternal() {
        return false;
    }
}
