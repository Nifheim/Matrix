package com.github.beelzebu.matrix.messaging.message;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.message.Message;
import com.github.beelzebu.matrix.api.messaging.message.MessageType;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.server.ServerInfoImpl;
import com.google.gson.JsonObject;

/**
 * @author Jaime Suárez
 */
public final class ServerUnregisterMessage extends Message {

    public ServerUnregisterMessage(ServerInfo serverInfo) {
        super(MessageType.SERVER_UNREGISTER);
        content = new JsonObject();
        content.add("serverInfo", Matrix.GSON.toJsonTree(serverInfo, ServerInfoImpl.class));
    }

    public static ServerInfo getServerInfo(Message message) {
        if (message.getContent() == null || !message.getContent().has("serverInfo")) {
            throw new IllegalArgumentException("Message doesn't contain serverInfo");
        }
        return Matrix.GSON.fromJson(message.getContent().get("serverInfo"), ServerInfoImpl.class);
    }
}
