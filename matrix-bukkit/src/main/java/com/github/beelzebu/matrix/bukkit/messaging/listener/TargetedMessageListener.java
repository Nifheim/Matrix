package com.github.beelzebu.matrix.bukkit.messaging.listener;

import com.github.beelzebu.matrix.api.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.messaging.MessageListener;
import com.github.beelzebu.matrix.api.messaging.message.Message;
import com.github.beelzebu.matrix.api.messaging.message.MessageType;
import com.github.beelzebu.matrix.messaging.message.TargetedMessage;
import org.bukkit.entity.Player;

/**
 * @author Beelzebu
 */
public class TargetedMessageListener extends MessageListener {

    private final MatrixBukkitBootstrap bootstrap;

    public TargetedMessageListener(MatrixBukkitBootstrap bootstrap) {
        super(MessageType.TARGETED_MESSAGE);
        this.bootstrap = bootstrap;
    }

    @Override
    public void onMessage(Message message) {
        Player player = bootstrap.getApi().getPlayerManager().getPlatformPlayer(TargetedMessage.getPlayer(message));
        if (player != null) {
            player.sendMessage(TargetedMessage.getMessage(message));
        }
    }
}
