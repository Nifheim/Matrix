package net.nifheim.matrix.common.messaging.message;

import java.util.UUID;
import net.nifheim.matrix.api.messaging.Message;
import org.jetbrains.annotations.NotNull;

public class LoginMessage extends Message {

    public LoginMessage(@NotNull UUID playerUniqueId, @NotNull String playerName) {
        super("login");
        content.addProperty("playerUniqueId", playerUniqueId.toString());
        content.addProperty("playerName", playerName);
    }

    public static UUID getPlayerUniqueId(Message message) {
        if (message.getContent() == null || !message.getContent().has("playerUniqueId")) {
            throw new IllegalArgumentException("Message doesn't contain playerUniqueId");
        }
        return UUID.fromString(message.getContent().get("playerUniqueId").getAsString());
    }

    public static String getPlayerName(Message message) {
        if (message.getContent() == null || !message.getContent().has("playerName")) {
            throw new IllegalArgumentException("Message doesn't contain playerName");
        }
        return message.getContent().get("playerName").getAsString();
    }
}
