package com.github.beelzebu.matrix.bukkit.util.placeholders;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class OnlinePlaceholders extends PlaceholderExpansion {

    private volatile long bungeeCount = 0;
    private final Set<String> trackedServers = new HashSet<>();
    private final LoadingCache<String, String> status = Caffeine.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).weakValues().build(key -> Matrix.getAPI().getCache().getServers(key).isEmpty() ? "OFFLINE" : "ONLINE");
    private final LoadingCache<String, Integer> groupCount = Caffeine.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).weakValues().build(key -> Matrix.getAPI().getCache().getOnlinePlayersInGroup(key).size());

    public OnlinePlaceholders(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            Iterator<String> it = trackedServers.iterator();
            while (it.hasNext()) {
                String key = it.next();
                status.refresh(key);
                groupCount.refresh(key);
            }
            bungeeCount = Matrix.getAPI().getCache().getOnlinePlayers().size();
        }, 0, 60);
    }

    @Override
    public @NotNull String getIdentifier() {
        return "matrix-online";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Beelzebu";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.startsWith("status_")) {
            return status.get(params.split("_")[1]);
        }
        if (params.equals("bungee")) {
            return String.valueOf(bungeeCount);
        }
        if (params.equals("current")) {
            int online = 0;
            online = Bukkit.getOnlinePlayers().stream().filter(player::canSee).map((_item) -> 1).reduce(online, Integer::sum);
            return String.valueOf(online);
        }
        if (params.startsWith("group_")) {
            return String.valueOf(groupCount.get(params.split("_")[1]));
        }
        return "UNKNOWN ONLINE PLACEHOLDER";
    }
}
