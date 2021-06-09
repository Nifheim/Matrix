package com.github.beelzebu.matrix.messaging.message;

import com.github.beelzebu.matrix.api.messaging.message.Message;
import com.github.beelzebu.matrix.api.messaging.message.MessageType;
import com.google.gson.JsonObject;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public final class TargetedMessage extends Message {

    public TargetedMessage(@NotNull UUID uniqueId, String message) {
        super(MessageType.TARGETED_MESSAGE);
        content = new JsonObject();
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
