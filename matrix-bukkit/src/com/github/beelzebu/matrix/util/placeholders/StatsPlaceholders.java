package com.github.beelzebu.matrix.util.placeholders;

import com.github.beelzebu.matrix.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.api.player.Statistic;
import com.github.beelzebu.matrix.api.player.TopEntry;
import com.github.beelzebu.matrix.database.MySQLStorage;
import com.github.beelzebu.matrix.util.bungee.BungeeServerTracker;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class StatsPlaceholders extends PlaceholderExpansion {

    private final MatrixAPI api = Matrix.getAPI();
    private final Cache<UUID, Map<String, Map<Statistic, Long>>> personalStats = Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
    private final Cache<Statistic, TopEntry[]> weekly = Caffeine.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
    private final Cache<Statistic, TopEntry[]> monthly = Caffeine.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
    private final Cache<Statistic, TopEntry[]> total = Caffeine.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();

    public StatsPlaceholders() {
        api.getPlugin().getBootstrap().getScheduler().asyncRepeating(() -> {
            for (Statistic statistic : Statistic.values()) {
                api.getSQLDatabase().getTopStatWeekly(api.getServerInfo().getGameType().getGameName(), statistic).thenAccept(topEntries -> weekly.put(statistic, topEntries));
                api.getSQLDatabase().getTopStatMonthly(api.getServerInfo().getGameType().getGameName(), statistic).thenAccept(topEntries -> monthly.put(statistic, topEntries));
                api.getSQLDatabase().getTopStatTotal(api.getServerInfo().getGameType().getGameName(), statistic).thenAccept(topEntries -> total.put(statistic, topEntries));
            }
        }, 10, TimeUnit.MINUTES);
    }

    @Override
    public String onPlaceholderRequest(Player p, String stat) {
        if (p == null) {
            return "Player needed!";
        }
        if (stat.equals("nick")) {
            return api.getPlayer(p.getUniqueId()).getDisplayName();
        }
        if (stat.startsWith("online")) {
            if (stat.matches("online_status_.*")) {
                return BungeeServerTracker.isOnline(stat.split("_")[1]) ? "ONLINE" : "OFFLINE";
            }
            if (stat.equals("onlinebungee")) {
                return String.valueOf(BungeeServerTracker.getTotalOnline());
            }
            if (stat.equals("onlineplayers")) {
                int online = 0;
                online = Bukkit.getOnlinePlayers().stream().filter(p::canSee).map((_item) -> 1).reduce(online, Integer::sum);
                return String.valueOf(online);
            }
            if (stat.matches("online_.*")) {
                return String.valueOf(BungeeServerTracker.getPlayersOnline(stat.split("_")[1]));
            }
        }
        // <server>_top_<stat>_<type>_<request>_<pos>
        // 0        1   2      3      4         5
        // towny_   top_weekly_kills_ name_     1
        String[] query = stat.split("_");
        String server = query[0];
        String statType = query[2];
        Statistic statistic = Statistic.valueOf(query[3].toUpperCase());
        String request = query[4];
        if (stat.startsWith(server)) {
            if (stat.startsWith(server + "_top_")) {
                int topPosition = Integer.parseInt(query[5]);
                if (topPosition < 1) {
                    topPosition = 1;
                }
                if (topPosition >= MySQLStorage.TOP_SIZE) {
                    topPosition = MySQLStorage.TOP_SIZE;
                }
                topPosition--; // we use array index here, so we need to start from 0
                switch (statType) {
                    case "weekly": {
                        TopEntry[] entries = weekly.getIfPresent(statistic);
                        if (entries != null && entries.length != 0) {
                            return getValue(entries[topPosition], request);
                        }
                    }
                    case "monthly": {
                        TopEntry[] entries = monthly.getIfPresent(statistic);
                        if (entries != null && entries.length != 0) {
                            return getValue(entries[topPosition], request);
                        }
                    }
                    default: {
                        TopEntry[] entries = total.getIfPresent(statistic);
                        if (entries != null && entries.length != 0) {
                            return getValue(entries[topPosition], request);
                        }
                    }
                }
            } else {
                // <server>_personal_<stat>_<type>_<request>
                // 0        1        2      3      4
                // towny_personal_kills_weekly_name
                if (personalStats.getIfPresent(p.getUniqueId()) == null) {
                    Matrix.getLogger().info("Populating stats cache for: " + p.getName());
                    return personalStats.get(p.getUniqueId(), uniqueId -> {
                        Map<String, Map<Statistic, Long>> statsMap = new HashMap<>();
                        String[] sTypes = {"weekly", "monthly", "total"};
                        for (String sType : sTypes) {
                            Map<Statistic, Long> values = new HashMap<>();
                            for (Statistic populatingStatistic : Statistic.values()) {
                                api.getSQLDatabase().getStat(api.getPlayer(p.getUniqueId()), api.getServerInfo().getGameType().getGameName(), statistic).thenAccept(value -> values.put(populatingStatistic, value));
                            }
                            statsMap.put(sType, values);
                        }
                        return statsMap;
                    }).getOrDefault(statType, new HashMap<>()).getOrDefault(statistic, 0L).toString();
                }
                return personalStats.getIfPresent(p.getUniqueId()).get(statType).get(statistic).toString();
            }
            return "no data available";
        }
        return "UNKNOWN STATISTIC OR SERVER";
    }

    private String getValue(TopEntry entry, String request) {
        switch (request) {
            case "position":
                return String.valueOf(entry.getPosition());
            case "value":
                return String.valueOf(entry.getValue());
            case "name":
            default:
                return entry.getName();
        }
    }

    @Override
    public String getIdentifier() {
        return "matrix";
    }

    @Override
    public String getAuthor() {
        return "Beelzebu";
    }

    @Override
    public String getVersion() {
        return MatrixBukkitBootstrap.getPlugin(MatrixBukkitBootstrap.class).getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }
}
