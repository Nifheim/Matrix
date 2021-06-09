package com.github.beelzebu.matrix.messaging.message;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.messaging.message.Message;
import com.github.beelzebu.matrix.api.messaging.message.MessageType;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.server.ServerInfoImpl;
import com.google.gson.JsonObject;

/**
 * @author Beelzebu
 */
public  final class ServerRegisterMessage extends Message {

    public ServerRegisterMessage(ServerInfo serverInfo, String address, int port) {
        super(MessageType.SERVER_REGISTER);
        content = new JsonObject();
        content.addProperty("address", address);
        content.addProperty("port", port);
        content.add("serverInfo", Matrix.GSON.toJsonTree(serverInfo, ServerInfoImpl.class));
    }

    public static ServerInfo getServerInfo(Message message) {
        if (message.getContent() == null || !message.getContent().has("serverInfo")) {
            throw new IllegalArgumentException("Message doesn't contain serverInfo");
        }
        return Matrix.GSON.fromJson(message.getContent().get("serverInfo"), ServerInfoImpl.class);
    }

    public static String getAddress(Message message) {
        if (message.getContent() == null || !message.getContent().has("address")) {
            throw new IllegalArgumentException("Message doesn't contain serverInfo");
        }
        return message.getContent().get("address").getAsString();
    }

    public static int getPort(Message message) {
        if (message.getContent() == null || !message.getContent().has("address")) {
            throw new IllegalArgumentException("Message doesn't contain serverInfo");
        }
        return message.getContent().get("port").getAsInt();
    }
}
