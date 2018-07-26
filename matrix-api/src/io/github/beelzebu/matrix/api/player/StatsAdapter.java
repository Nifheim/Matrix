package io.github.beelzebu.matrix.api.player;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/**
 * @author Beelzebu
 */
public class StatsAdapter extends TypeAdapter<IStatistics> {

    @Override
    public void write(JsonWriter out, IStatistics value) throws IOException {
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
    public IStatistics read(JsonReader in) throws IOException {
        in.beginObject();
        String server = in.nextString();
        int pkills = in.nextInt(), mobkills = in.nextInt(), deaths = in.nextInt(), broken = in.nextInt(), placed = in.nextInt();
        in.endObject();
        return new IStatistics() {
            @Override
            public String getServer() {
                return server;
            }

            @Override
            public int getPlayerKills() {
                return pkills;
            }

            @Override
            public int getMobKills() {
                return mobkills;
            }

            @Override
            public int getDeaths() {
                return deaths;
            }

            @Override
            public int getBlocksBroken() {
                return broken;
            }

            @Override
            public int getBlocksPlaced() {
                return placed;
            }
        };
    }
}
