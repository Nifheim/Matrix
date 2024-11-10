package net.nifheim.matrix.common.messaging.message;

import net.nifheim.matrix.api.messaging.message.Message;
import net.nifheim.matrix.api.messaging.message.StandardChannel;
import net.nifheim.matrix.api.server.ServerInfo;
import net.nifheim.matrix.common.api.MatrixCommon;
import net.nifheim.matrix.common.server.ServerInfoImpl;

/**
 * @author Jaime Suárez
 */
public final class ServerUnregisterMessage extends Message {

    public ServerUnregisterMessage(ServerInfo serverInfo) {
        super(StandardChannel.SERVER_UNREGISTER);
        content.add("serverInfo", MatrixCommon.GSON.toJsonTree(serverInfo, ServerInfoImpl.class));
    }

    public static ServerInfo getServerInfo(Message message) {
        if (message.getContent() == null || !message.getContent().has("serverInfo")) {
            throw new IllegalArgumentException("Message doesn't contain serverInfo");
        }
        return MatrixCommon.GSON.fromJson(message.getContent().get("serverInfo"), ServerInfoImpl.class);
    }
}
