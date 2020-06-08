package com.github.beelzebu.matrix.motd;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.countdown.Countdown;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import redis.clients.jedis.Jedis;

/**
 * @author Beelzebu
 */
public final class MotdManager {

    private static final Random RANDOM = new Random();
    private static final List<Motd> MOTD_LIST = new ArrayList<>();
    private static final Cache<String, Countdown> COUNTDOWN_CACHE = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build();

    public static void onEnable() {
        MatrixAPI api = Matrix.getAPI();
        api.getConfig().getKeys("Motds").stream().map(key -> new Motd(key, api.getConfig().getStringList("Motds." + key + ".Lines"), api.getConfig().getString("Motds." + key + ".Countdown", null))).forEach(MOTD_LIST::add);
    }

    public static List<Motd> getMotdList() {
        return ImmutableList.copyOf(MOTD_LIST);
    }

    public static Countdown getCountdown(String id) {
        Countdown cachedCountdown = COUNTDOWN_CACHE.getIfPresent(id);
        if (cachedCountdown != null) {
            return cachedCountdown;
        }
        try (Jedis jedis = Matrix.getAPI().getMessaging().getPool().getResource()) {
            if (!jedis.exists("countdown:" + id)) {
                return null;
            }
            Countdown countdown = Matrix.GSON.fromJson(jedis.get("countdown:" + id), Countdown.class);
            COUNTDOWN_CACHE.put(id, countdown);
            return countdown;
        }
    }

    public static Motd getRandomMotd() {
        return getRandomMotd(true);
    }

    public static Motd getRandomMotd(boolean firstAttempt) {
        Motd motd = null;
        for (Motd value : MOTD_LIST) {
            if (value.getCountdown() != null) {
                if (!value.getCountdown().isOver() && RANDOM.nextBoolean()) {
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
