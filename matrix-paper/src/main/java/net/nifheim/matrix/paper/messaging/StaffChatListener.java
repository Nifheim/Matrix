package net.nifheim.matrix.paper.messaging;

import net.nifheim.matrix.api.messaging.MessageListener;
import net.nifheim.matrix.api.messaging.message.StandardChannel;
import net.nifheim.matrix.common.messaging.message.StaffChatMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Jaime Suárez
 */
public class StaffChatListener extends MessageListener<StaffChatMessage> {

    public StaffChatListener() {
        super(StandardChannel.MESSAGE_BROADCAST);
    }

    @Override
    public void onMessage(StaffChatMessage message) {
        String permission = StaffChatMessage.getPermission(message);
        String sMessage = StaffChatMessage.getMessage(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission(permission)) {
                continue;
            }
            player.sendMessage(sMessage);
        }
    }
}
