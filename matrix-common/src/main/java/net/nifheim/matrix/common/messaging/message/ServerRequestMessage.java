package net.nifheim.matrix.common.messaging.message;

import net.nifheim.matrix.api.messaging.message.Message;
import net.nifheim.matrix.api.messaging.message.StandardChannel;

/**
 * @author Jaime Suárez
 */
public  final class ServerRequestMessage extends Message {

    public ServerRequestMessage() {
        super(StandardChannel.SERVER_REQUEST);
    }
}
