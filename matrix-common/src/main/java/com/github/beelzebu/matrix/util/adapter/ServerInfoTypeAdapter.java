package com.github.beelzebu.matrix.util.adapter;

import com.github.beelzebu.matrix.api.player.GameMode;
import com.github.beelzebu.matrix.api.server.ServerInfo;
import com.github.beelzebu.matrix.api.server.ServerType;
import com.github.beelzebu.matrix.server.ServerInfoImpl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jaime Suárez
 */
public class ServerInfoTypeAdapter extends TypeAdapter<ServerInfo> {

    @Override
    public void write(@NotNull JsonWriter out, @NotNull ServerInfo value) throws IOException {
        out.beginObject().name("groupName").value(value.getGroupName())
                .name("serverName").value(value.getServerName())
                .name("serverType").value(value.getServerType().name())
                .name("gameMode").value(value.getDefaultGameMode().toString())
                .name("unique").value(value.isUnique())
                .name("lobby").value(value.getLobbyServer().join())
                .endObject();
    }

    @Override
    public @NotNull ServerInfo read(@NotNull JsonReader in) {
        JsonObject jsonObject = new JsonParser().parse(in).getAsJsonObject();
        return new ServerInfoImpl(
                ServerType.valueOf(jsonObject.get("serverType").getAsString()),
                jsonObject.get("groupName").getAsString(),
                jsonObject.get("serverName").getAsString(),
                GameMode.valueOf(jsonObject.get("gameMode").getAsString()),
                jsonObject.get("unique").getAsBoolean(),
                jsonObject.has("lobby") ? jsonObject.get("lobby").getAsString() : null,
                true
        );
    }
}
