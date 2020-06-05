package com.github.beelzebu.matrix.motd;

import com.google.common.collect.ImmutableList;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.countdown.Countdown;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import redis.clients.jedis.Jedis;

/**
 * @author Beelzebu
 */
public final class MotdManager {

    private static final Random r = new Random();
    private static final List<Motd> MOTDS = new ArrayList<>();

    public static void onEnable() {
        MatrixAPI api = Matrix.getAPI();
        api.getConfig().getKeys("Motds").stream().map(key -> new Motd(key, api.getConfig().getStringList("Motds." + key + ".Lines"), api.getConfig().getString("Motds." + key + ".Countdown", null))).forEach(MOTDS::add);
    }

    public static List<Motd> getMotds() {
        return ImmutableList.copyOf(MOTDS);
    }

    public static Countdown getCountdown(String id) {
        try (Jedis jedis = Matrix.getAPI().getMessaging().getPool().getResource()) {
            if (!jedis.exists("countdown:" + id)) {
                return null;
            }
            return Matrix.GSON.fromJson(jedis.get("countdown:" + id), Countdown.class);
        }
    }

    public static Motd getRandomMotd() {
        return getRandomMotd(true);
    }

    public static Motd getRandomMotd(boolean firstAttempt) {
        Motd motd = null;
        for (Motd value : MOTDS) {
            if (value.getCountdown() != null) {
                if (!value.getCountdown().isOver() && r.nextBoolean()) {
                    motd = value;
                    break;
                }
            }
            if (Objects.equals(value.getId(), "default")) {
                if (firstAttempt) {
                    continue;
                }
                motd = value;
                break;
            }
        }
        if (motd == null) {
            return getRandomMotd(false);
        }
        return motd;
    }
}
