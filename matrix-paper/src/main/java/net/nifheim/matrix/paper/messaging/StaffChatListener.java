package net.nifheim.matrix.paper.messaging;

import java.util.function.BiConsumer;
import net.nifheim.matrix.common.messaging.message.StaffChatMessage;
import net.nifheim.matrix.common.messaging.rabbitmq.AbstractRabbitMQConsumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Jaime Suárez
 */
public class StaffChatListener implements BiConsumer<StaffChatMessage, AbstractRabbitMQConsumer.DeliveryContext> {

    @Override
    public void accept(StaffChatMessage message, AbstractRabbitMQConsumer.DeliveryContext context) {
        String permission = StaffChatMessage.getPermission(message);
        String sMessage = StaffChatMessage.getMessage(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission(permission)) {
                continue;
            }
            player.sendMessage(sMessage);
        }
        try {
            context.acknowledge();
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error acknowledging staff chat message: " + e.getMessage());
        }
    }
}
