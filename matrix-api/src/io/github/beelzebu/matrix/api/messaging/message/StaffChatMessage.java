package io.github.beelzebu.matrix.api.messaging.message;

import io.github.beelzebu.matrix.api.Matrix;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

/**
 * @author Beelzebu
 */
@Getter
@EqualsAndHashCode(callSuper = true)
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

    @Override
    protected boolean onlyExternal() {
        return false;
    }

    @Override
    public void read() {
        if (api.isBungee()) {
            api.getPlayers().stream().filter(p -> api.hasPermission(p, getPermission())).forEach(p -> api.getPlugin().sendMessage(p.getUniqueId(), getMessage()));
            Matrix.getLogger().info(getMessage());
        }
    }
}
