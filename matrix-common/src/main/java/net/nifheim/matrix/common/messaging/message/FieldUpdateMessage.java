package net.nifheim.matrix.common.messaging.message;

import java.util.Objects;
import net.nifheim.matrix.api.messaging.message.Message;
import net.nifheim.matrix.api.messaging.message.StandardChannel;
import net.nifheim.matrix.common.api.MatrixCommon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Jaime Suárez
 */
public final class FieldUpdateMessage extends Message {

    public FieldUpdateMessage(@NotNull String hexId, @NotNull String field, @Nullable Object value, @Nullable Class<?> type) {
        super(StandardChannel.UPDATE_FIELD);
        content.addProperty("hexId", hexId);
        content.addProperty("field", field);
        content.add("value", value != null ? MatrixCommon.GSON.toJsonTree(value, Objects.requireNonNull(type)) : null);
    }

    public String getPlayerId() {
        if (this.getContent() == null || !this.getContent().has("hexId")) {
            throw new IllegalArgumentException("Message doesn't contain hexId");
        }
        return this.getContent().get("hexId").getAsString();
    }

    public String getField() {
        if (this.getContent() == null || !this.getContent().has("field")) {
            throw new IllegalArgumentException("Message doesn't contain field");
        }
        return this.getContent().get("field").getAsString();
    }

    public <T> T getValue(Class<T> type) {
        if (this.getContent() == null || !this.getContent().has("value")) {
            throw new IllegalArgumentException("Message doesn't contain value");
        }
        return MatrixCommon.GSON.fromJson(this.getContent().get("value"), type);
    }

    public String getRawValue() {
        if (this.getContent() == null || !this.getContent().has("value")) {
            throw new IllegalArgumentException("Message doesn't contain value");
        }
        return this.getContent().get("value").getAsString();
    }
}
