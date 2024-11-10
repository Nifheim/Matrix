package net.nifheim.matrix.api.messaging.message;

import com.google.gson.JsonObject;
import java.util.UUID;
import net.nifheim.matrix.api.MatrixProvider;
import net.nifheim.matrix.api.server.ServerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a message to be sent using a message broker, its content is defined by a json object, a random UUID is
 * generated for every message, so they can be identified by the consumers.
 *
 * @author Jaime Suárez
 */
public class Message {

    private final String source = MatrixProvider.getAPI().getServerInfo().getName();
    private final UUID uniqueId = UUID.randomUUID();
    private final String channel;
    protected final JsonObject content;

    public Message(@NotNull String channel, @NotNull JsonObject content) {
        this.channel = channel;
        this.content = content;
    }

    public Message(@NotNull String channel) {
        this(channel, new JsonObject());
    }

    public Message(@NotNull StandardChannel channel) {
        this(channel.getChannelName());
    }

    public Message(@NotNull StandardChannel channel, @NotNull JsonObject content) {
        this(channel.getChannelName(), content);
    }

    /**
     * Retrieves the source of the message.
     *
     * @return the source of the message as a {@link String}.
     */
    public final @NotNull String getSource() {
        return source;
    }

    /**
     * Get the message channel, every message can be handled by different ways by the consumers and that is delegated to
     * the implementations of this API.
     *
     * @return the channel where this message will be sent and received.
     */
    public final @NotNull String getChannel() {
        return channel;
    }

    /**
     * Get the json content of this message
     *
     * @return a {@link JsonObject} with the content of this message.
     */
    public @Nullable JsonObject getContent() {
        return content;
    }

    /**
     * Get the unique id generated for this message, every id uuid is generated on the creation of the instance, so
     * there can't be messages with the same uuid
     *
     * @return the {@link UUID} generated for this message
     */
    public final @NotNull UUID getUniqueId() {
        return uniqueId;
    }
}
