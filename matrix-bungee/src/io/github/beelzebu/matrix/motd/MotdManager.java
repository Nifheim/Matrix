package io.github.beelzebu.matrix.motd;

import com.google.common.collect.ImmutableList;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.countdown.Countdown;
import java.util.ArrayList;
import java.util.List;
import redis.clients.jedis.Jedis;

/**
 * @author Beelzebu
 */
public final class MotdManager {

    private static final List<Motd> MOTDS = new ArrayList<>();

    public static void onEnable() {
        MatrixAPI api = Matrix.getAPI();
        api.getConfig().getKeys("Motds").stream().map(key -> new Motd(key, api.getConfig().getStringList("Motds." + key + ".Lines"), api.getConfig().getString("Motds." + key + ".Countdown", null))).forEach(MOTDS::add);
    }

    public static List<Motd> getMotds() {
        return ImmutableList.copyOf(MOTDS);
    }

    public static Countdown getCountdown(String id) {
        try (Jedis jedis = Matrix.getAPI().getRedis().getPool().getResource()) {
            if (!jedis.exists("countdown:" + id)) {
                return null;
            }
            return Matrix.GSON.fromJson(jedis.get("countdown:" + id), Countdown.class);
        }
    }
}
