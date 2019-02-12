package io.github.beelzebu.matrix.api.messaging.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.api.ChatColor;

/**
 * @author Beelzebu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffChatMessage extends RedisMessage {

    private final String permission;
    private final String message;

    @Override
    public String getChannel() {
        return "api-staff-chat";
    }

    @Override
    public void read() {
        if (api.isBungee()) {
            api.getPlayers().stream().filter(p -> api.hasPermission(p, getPermission())).forEach(p -> api.getPlugin().sendMessage(p.getUniqueId(), getMessage()));
            api.log(getMessage());
        }
    }

    @Override
    protected boolean onlyExternal() {
        return false;
    }

    public String getMessage() {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
