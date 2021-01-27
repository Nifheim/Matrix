package com.github.beelzebu.matrix.bukkit.util.placeholders;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class OnlinePlaceholders extends PlaceholderExpansion {

    private volatile int bungeeCount = 0;
    private final LoadingCache<String, String> status = Caffeine.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).weakValues().build(new CacheLoader<String, String>() {
        @Override
        public @Nullable String load(@NonNull String key) throws Exception {
            return Matrix.getAPI().getServerManager().getServers(key).thenApply(serverInfos -> {
                if (serverInfos.isEmpty()) {
                    return "OFFLINE";
                } else {
                    return "ONLINE";
                }
            }).join();
        }

        @Override
        public @NonNull CompletableFuture<String> asyncLoad(@NonNull String key, @NonNull Executor executor) {
            return Matrix.getAPI().getServerManager().getServers(key).thenApply(serverInfos -> {
                if (serverInfos.isEmpty()) {
                    return "OFFLINE";
                } else {
                    return "ONLINE";
                }
            });
        }
    });
    private final LoadingCache<String, Integer> groupCount = Caffeine.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).weakValues().build(new CacheLoader<String, Integer>() {
        @Override
        public @Nullable Integer load(@NonNull String key) throws Exception {
            return Matrix.getAPI().getPlayerManager().getOnlinePlayerCountInGroup(key).join();
        }

        @Override
        public @NonNull CompletableFuture<Integer> asyncLoad(@NonNull String key, @NonNull Executor executor) {
            return Matrix.getAPI().getPlayerManager().getOnlinePlayerCountInGroup(key);
        }
    });

    public OnlinePlaceholders(Plugin plugin) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> Matrix.getAPI().getPlayerManager().getOnlinePlayerCount().thenAccept(count -> bungeeCount = count), 0, 60);
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
            String server = params.replaceFirst("status_", "");
            status.refresh(server);
            return status.get(server);
        }
        if (params.equals("bungee")) {
            return String.valueOf(bungeeCount);
        }
        if (params.equals("current")) {
            int online = 0;
            online = Bukkit.getOnlinePlayers().stream().filter(player::canSee).map(i -> 1).reduce(online, Integer::sum);
            return String.valueOf(online);
        }
        if (params.startsWith("group_")) {
            String group = params.replace("group_", "");
            groupCount.refresh(group);
            return String.valueOf(groupCount.get(group));
        }
        return "UNKNOWN ONLINE PLACEHOLDER";
    }
}
