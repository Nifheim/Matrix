package com.github.beelzebu.matrix.bukkit.util.placeholders.stats;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixBukkitAPI;
import com.github.beelzebu.matrix.api.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.player.Statistic;
import com.github.beelzebu.matrix.api.player.TopEntry;
import com.github.beelzebu.matrix.database.StorageProvider;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class StatsPlaceholders extends PlaceholderExpansion {

    private final MatrixBukkitAPI api;
    private final Cache<UUID, Map<String, Map<Statistic, Long>>> personalStats = Caffeine.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
    private final Cache<Statistic, TopEntry[]> weekly = Caffeine.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
    private final Cache<Statistic, TopEntry[]> monthly = Caffeine.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
    private final Cache<Statistic, TopEntry[]> total = Caffeine.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();

    public StatsPlaceholders(MatrixBukkitAPI api) {
        this.api = api;
        api.getPlugin().getBootstrap().getScheduler().asyncRepeating(() -> {
            for (Statistic statistic : Statistic.values()) {
                api.getDatabase().getTopStatWeekly(api.getServerInfo().getGroupName(), statistic).thenAccept(topEntries -> weekly.put(statistic, topEntries));
                api.getDatabase().getTopStatMonthly(api.getServerInfo().getGroupName(), statistic).thenAccept(topEntries -> monthly.put(statistic, topEntries));
                api.getDatabase().getTopStatTotal(api.getServerInfo().getGroupName(), statistic).thenAccept(topEntries -> total.put(statistic, topEntries));
            }
        }, 10, TimeUnit.MINUTES);
    }

    @Override
    public String onPlaceholderRequest(Player p, String stat) {
        if (p == null) {
            return "Player needed!";
        }
        if (stat.equals("nick")) {
            return api.getPlayerManager().getPlayer(p).join().getDisplayName();
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
                if (topPosition >= StorageProvider.TOP_SIZE) {
                    topPosition = StorageProvider.TOP_SIZE;
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
                                api.getDatabase().getStatById(api.getPlayerManager().getMetaInjector().getId(p), api.getServerInfo().getGroupName(), statistic).thenAccept(value -> values.put(populatingStatistic, value));
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
}
