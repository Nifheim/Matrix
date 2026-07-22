package net.nifheim.matrix.paper.messaging;

import java.util.function.BiConsumer;
import net.nifheim.matrix.common.messaging.message.TargetedMessage;
import net.nifheim.matrix.common.messaging.rabbitmq.AbstractRabbitMQConsumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Jaime Suárez
 */
public class TargetedMessageListener implements BiConsumer<TargetedMessage, AbstractRabbitMQConsumer.DeliveryContext> {

    @Override
    public void accept(TargetedMessage message, AbstractRabbitMQConsumer.DeliveryContext context) {
        Player player = Bukkit.getPlayer(TargetedMessage.getPlayer(message));
        if (player != null) {
            player.sendMessage(TargetedMessage.getMessage(message));
        }
        try {
            context.acknowledge();
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error acknowledging targeted message: " + e.getMessage());
        }
    }
}
