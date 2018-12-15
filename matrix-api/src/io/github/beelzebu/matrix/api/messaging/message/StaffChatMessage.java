package io.github.beelzebu.matrix.api.messaging.message;

import lombok.Data;
import net.md_5.bungee.api.ChatColor;

/**
 * @author Beelzebu
 */
@Data
public class StaffChatMessage implements RedisMessage {

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
