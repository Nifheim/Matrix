package net.nifheim.matrix.paper.messaging;

import net.nifheim.matrix.api.messaging.MessageListener;
import net.nifheim.matrix.api.messaging.message.StandardChannel;
import net.nifheim.matrix.common.messaging.message.TargetedMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Jaime Suárez
 */
public class TargetedMessageListener extends MessageListener<TargetedMessage> {

    public TargetedMessageListener() {
        super(StandardChannel.MESSAGE_TARGETED);
    }

    @Override
    public void onMessage(TargetedMessage message) {
        Player player = Bukkit.getPlayer(TargetedMessage.getPlayer(message));
        if (player != null) {
            player.sendMessage(TargetedMessage.getMessage(message));
        }
    }
}
