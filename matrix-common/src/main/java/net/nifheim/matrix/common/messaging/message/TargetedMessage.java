package net.nifheim.matrix.common.messaging.message;

import java.util.UUID;
import net.nifheim.matrix.api.messaging.Message;
import net.nifheim.matrix.api.messaging.StandardChannel;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jaime Suárez
 */
public final class TargetedMessage extends Message {

    public TargetedMessage(@NotNull UUID uniqueId, String message) {
        super(StandardChannel.MESSAGE_TARGETED);
        content.addProperty("player", uniqueId.toString());
        content.addProperty("message", message);
    }

    public static UUID getPlayer(Message message) {
        if (message.getContent() == null || !message.getContent().has("player")) {
            throw new IllegalArgumentException("Message doesn't contain player");
        }
        return UUID.fromString(message.getContent().get("player").getAsString());
    }

    public static String getMessage(Message message) {
        if (message.getContent() == null || !message.getContent().has("message")) {
            throw new IllegalArgumentException("Message doesn't contain message");
        }
        return message.getContent().get("message").getAsString();
    }
}
