package net.nifheim.matrix.common.util.adapter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Locale;
import net.nifheim.matrix.api.player.gamemode.GameMode;
import net.nifheim.matrix.api.server.ServerInfo;
import net.nifheim.matrix.api.server.ServerType;
import net.nifheim.matrix.common.server.ServerInfoImpl;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jaime Suárez
 */
public class ServerInfoTypeAdapter extends TypeAdapter<ServerInfo> {

    @Override
    public void write(@NotNull JsonWriter out, @NotNull ServerInfo value) throws IOException {
        out.beginObject().name("groupName").value(value.getGroupName())
                .name("rawServerName").value(value.getRawName())
                .name("serverNumber").value(value.getServerNumber())
                .name("serverType").value(value.getServerType().name())
                .name("gameMode").value(value.getDefaultGameMode().toString())
                .name("address").value(value.getAddress())
                .name("port").value(value.getPort())
                .name("maxPlayers").value(value.getMaxPlayers())
                .endObject();
    }

    @Override
    public @NotNull ServerInfo read(@NotNull JsonReader in) {
        JsonObject jsonObject = JsonParser.parseReader(in).getAsJsonObject();
        String rawServerName = jsonObject.has("rawServerName") ? jsonObject.get("rawServerName").getAsString() : null;
        int serverNumber = jsonObject.has("serverNumber") ? jsonObject.get("serverNumber").getAsInt() : -1;
        return new ServerInfoImpl(
                jsonObject.get("groupName").getAsString(),
                rawServerName,
                serverNumber,
                ServerType.valueOf(jsonObject.get("serverType").getAsString().toUpperCase(Locale.ROOT)),
                GameMode.valueOf(jsonObject.get("gameMode").getAsString().toUpperCase(Locale.ROOT)),
                jsonObject.get("address").getAsString(),
                jsonObject.get("port").getAsInt(),
                jsonObject.get("maxPlayers").getAsInt()
        );
    }
}
