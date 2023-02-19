package com.github.beelzebu.matrix.messaging.message;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.message.Message;
import com.github.beelzebu.matrix.api.messaging.message.MessageType;
import com.google.gson.JsonObject;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Beelzebu
 */
public final class FieldUpdateMessage extends Message {

    public FieldUpdateMessage(@NotNull String hexId, @NotNull String field, @Nullable Object value, @Nullable Class<?> type) {
        super(MessageType.FIELD_UPDATE);
        content = new JsonObject();
        content.addProperty("hexId", hexId);
        content.addProperty("field", field);
        content.add("value", value != null ? Matrix.GSON.toJsonTree(value, Objects.requireNonNull(type)) : null);
    }

    public static String getPlayerId(Message message) {
        if (message.getContent() == null || !message.getContent().has("hexId")) {
            throw new IllegalArgumentException("Message doesn't contain hexId");
        }
        return message.getContent().get("hexId").getAsString();
    }

    public static String getField(Message message) {
        if (message.getContent() == null || !message.getContent().has("field")) {
            throw new IllegalArgumentException("Message doesn't contain field");
        }
        return message.getContent().get("field").getAsString();
    }

    public static <T> T getValue(Message message, Class<T> type) {
        if (message.getContent() == null || !message.getContent().has("value")) {
            throw new IllegalArgumentException("Message doesn't contain value");
        }
        return Matrix.GSON.fromJson(message.getContent().get("value"), type);
    }
}
