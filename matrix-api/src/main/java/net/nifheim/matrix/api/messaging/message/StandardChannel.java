package net.nifheim.matrix.api.messaging.message;

/**
 * Enum for default channels used to send and request information across matrix instances.
 *
 * @author Jaime Suárez
 */
public enum StandardChannel {

    EXECUTE_COMMAND("matrix:command"),
    UPDATE_FIELD("matrix:field"),
    MESSAGE_TARGETED("message:targeted"),
    MESSAGE_BROADCAST("message:broadcast"),
    SERVER_REGISTER("server:register"),
    SERVER_UNREGISTER("server:unregister"),
    SERVER_REQUEST("server:request"),
    UNDEFINED("undefined");

    private final String channel;

    StandardChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Get the channel name, this method has the same effect as using {@link #toString()}
     *
     * @return channel name.
     */
    public String getChannelName() {
        return channel;
    }

    @Override
    public String toString() {
        return channel;
    }
}
