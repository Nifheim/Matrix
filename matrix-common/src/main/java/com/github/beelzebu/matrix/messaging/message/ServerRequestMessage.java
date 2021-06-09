package com.github.beelzebu.matrix.messaging.message;

import com.github.beelzebu.matrix.api.messaging.message.Message;
import com.github.beelzebu.matrix.api.messaging.message.MessageType;

/**
 * @author Beelzebu
 */
public  final class ServerRequestMessage extends Message {

    public ServerRequestMessage() {
        super(MessageType.SERVER_REQUEST);
    }
}
