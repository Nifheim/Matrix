package com.github.beelzebu.matrix.messaging.message;

import com.github.beelzebu.matrix.api.messaging.message.Message;
import com.github.beelzebu.matrix.api.messaging.message.MessageType;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public final class StaffChatMessage extends Message {

    public StaffChatMessage(@NotNull String permission, @NotNull String message) {
        super(MessageType.STAFF_CHAT);
        content = new JsonObject();
        content.addProperty("permission", permission);
        content.addProperty("message", message);
    }

    public static String getPermission(Message message) {
        if (message.getContent() == null || !message.getContent().has("permission")) {
            throw new IllegalArgumentException("Message doesn't contain permission");
        }
        return message.getContent().get("permission").getAsString();
    }

    public static String getMessage(Message message) {
        if (message.getContent() == null || !message.getContent().has("message")) {
            throw new IllegalArgumentException("Message doesn't contain message");
        }
        return message.getContent().get("message").getAsString();
    }
}
