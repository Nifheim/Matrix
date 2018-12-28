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

    public String getMessage() {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
