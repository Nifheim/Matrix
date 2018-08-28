package io.github.beelzebu.matrix.api.player;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/**
 * @author Beelzebu
 */
public class StatsAdapter extends TypeAdapter<Statistics> {

    @Override
    public void write(JsonWriter out, Statistics value) throws IOException {
        out.beginObject().
                name("server").value(value.getServer()).
                name("pkills").value(value.getPlayerKills()).
                name("mkills").value(value.getMobKills()).
                name("deaths").value(value.getDeaths()).
                name("broken").value(value.getBlocksBroken()).
                name("placed").value(value.getBlocksPlaced()).
                endObject();
    }

    @Override
    public Statistics read(JsonReader in) throws IOException {
        in.beginObject();
        String server = in.nextString();
        int pkills = in.nextInt(),
                mobkills = in.nextInt(),
                deaths = in.nextInt(),
                broken = in.nextInt(),
                placed = in.nextInt();
        in.endObject();
        return new Statistics(server, pkills, mobkills, deaths, broken, placed);
    }
}
