package com.github.beelzebu.matrix.motd;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.countdown.Countdown;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Beelzebu
 */
public final class MotdManager {

    private static final Random RANDOM = new Random();
    private static final Set<Motd> FORCED_MOTD = new HashSet<>();
    private static final List<Motd> MOTD_LIST = new ArrayList<>();
    private static final Cache<String, Countdown> COUNTDOWN_CACHE = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build();

    public static void onEnable() {
        MatrixAPI api = Matrix.getAPI();
        MOTD_LIST.clear();
        FORCED_MOTD.clear();
        api.getConfig().getKeys("Motds").stream().map(key -> new Motd(key, api.getConfig().getStringList("Motds." + key + ".Lines"), api.getConfig().getString("Motds." + key + ".Countdown", null))).forEach(MOTD_LIST::add);
        api.getConfig().getKeys("Forced Motds").stream().map(key -> new Motd(key.replace(',', '.'), api.getConfig().getStringList("Forced Motds." + key + ".Lines"), null)).forEach(FORCED_MOTD::add);
    }

    public static List<Motd> getMotdList() {
        return ImmutableList.copyOf(MOTD_LIST);
    }

    public static Countdown getCountdown(String id) {
        Countdown cachedCountdown = COUNTDOWN_CACHE.getIfPresent(id);
        if (cachedCountdown != null) {
            return cachedCountdown;
        }
        return null;
        // TODO: enable
        /*
        try (Jedis jedis = RedisManager.getInstance().getPool().getResource()) {
            if (!jedis.exists("countdown:" + id)) {
                return null;
            }
            Countdown countdown = Matrix.GSON.fromJson(jedis.get("countdown:" + id), Countdown.class);
            COUNTDOWN_CACHE.put(id, countdown);
            return countdown;
        }*/
    }

    public static Motd getRandomMotd() {
        return getRandomMotd(true);
    }

    public static Motd getForcedMotd(String host) {
        for (Motd motd : FORCED_MOTD) {
            if (Objects.equals(motd.getId(), host)) {
                return motd;
            }
        }
        return null;
    }

    public static Motd getRandomMotd(boolean firstAttempt) {
        Motd motd = null;
        for (Motd value : MOTD_LIST) {
            if (RANDOM.nextBoolean()) {
                if (value.getCountdown() != null) {
                    if (!value.getCountdown().isOver()) {
                        motd = value;
                        break;
                    }
                }
                if (firstAttempt && Objects.equals(value.getId(), "default")) {
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
