package com.github.beelzebu.matrix.bukkit.messaging.listener;

import com.github.beelzebu.matrix.api.messaging.MessageListener;
import com.github.beelzebu.matrix.api.messaging.message.Message;
import com.github.beelzebu.matrix.api.messaging.message.MessageType;
import com.github.beelzebu.matrix.messaging.message.StaffChatMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Jaime Suárez
 */
public class StaffChatListener extends MessageListener {

    public StaffChatListener() {
        super(MessageType.STAFF_CHAT);
    }

    @Override
    public void onMessage(Message message) {
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
