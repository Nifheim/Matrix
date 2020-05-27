package io.github.beelzebu.matrix.api.messaging.message;

import io.github.beelzebu.matrix.api.Matrix;
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

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof io.github.beelzebu.matrix.api.messaging.message.StaffChatMessage)) {
            return false;
        }
        io.github.beelzebu.matrix.api.messaging.message.StaffChatMessage other = (io.github.beelzebu.matrix.api.messaging.message.StaffChatMessage) o;
        if (!other.canEqual((java.lang.Object) this)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        java.lang.Object this$permission = getPermission();
        java.lang.Object other$permission = other.getPermission();
        if (this$permission == null ? other$permission != null : !this$permission.equals(other$permission)) {
            return false;
        }
        java.lang.Object this$message = getMessage();
        java.lang.Object other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        java.lang.Object $permission = getPermission();
        result = result * PRIME + ($permission == null ? 43 : $permission.hashCode());
        java.lang.Object $message = getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        return result;
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

    protected boolean canEqual(Object other) {
        return other instanceof io.github.beelzebu.matrix.api.messaging.message.StaffChatMessage;
    }
}
