package net.nifheim.matrix.api.messaging;

import net.nifheim.matrix.api.messaging.message.Message;
import net.nifheim.matrix.api.messaging.message.StandardChannel;

/**
 * A message listener is defined to consume messages in a specific channel.
 *
 * @author Jaime Suárez
 */
public abstract class MessageListener <T extends Message> {

    private final String channel;

    public MessageListener(String channel) {
        this.channel = channel;
    }

    public MessageListener(StandardChannel channel) {
        this(channel.getChannelName());
    }

    /**
     * Get the message channel being consumed by this listener.
     *
     * @return The channel being consumed by this listener.
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Consume the message received by the message broker
     *
     * @param message the message to be consumed
     */
    public abstract void onMessage(T message);
}
