package net.nifheim.matrix.common.messaging.message;

import net.nifheim.matrix.api.messaging.message.Message;
import net.nifheim.matrix.api.messaging.message.StandardChannel;
import net.nifheim.matrix.api.server.ServerInfo;
import net.nifheim.matrix.common.api.MatrixCommon;
import net.nifheim.matrix.common.server.ServerInfoImpl;

/**
 * @author Jaime Suárez
 */
public final class ServerRegisterMessage extends Message {

    public ServerRegisterMessage(ServerInfo serverInfo) {
        super(StandardChannel.SERVER_REGISTER);
        content.add("serverInfo", MatrixCommon.GSON.toJsonTree(serverInfo, ServerInfoImpl.class));
    }

    public ServerInfo getServerInfo() {
        if (this.getContent() == null || !this.getContent().has("serverInfo")) {
            throw new IllegalArgumentException("Message doesn't contain serverInfo");
        }
        return MatrixCommon.GSON.fromJson(this.getContent().get("serverInfo"), ServerInfoImpl.class);
    }
}
