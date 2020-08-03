package com.github.beelzebu.matrix.util.placeholders;

import com.github.beelzebu.matrix.MatrixBukkitBootstrap;
import cl.indiopikaro.jmatrix.api.Matrix;
import cl.indiopikaro.jmatrix.api.MatrixAPI;
import com.github.beelzebu.matrix.util.bungee.BungeeServerTracker;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class StatsPlaceholders extends PlaceholderExpansion {

    private final MatrixAPI api = Matrix.getAPI();

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
        String server = stat.split("_")[0];
        if (stat.startsWith(server)) {
            String finalstat;
            if (stat.startsWith(server + "_top_")) {
                //finalstat = stat.replaceAll(server + "_top_", "");
                //int i = Integer.parseInt(finalstat.substring(finalstat.length() - 1));
                //finalstat = finalstat.replaceAll(finalstat.substring(finalstat.length() - 2), "");
                //List<String> toplist = api.getMySQL().getTop(finalstat, server, 10);
                return "Top de estadísticas deshabilitado temporalmente.";//toplist.get(i - 1);
            } else {
                finalstat = stat.replaceAll(server + "_", "");
                return "Estadísticas deshabilitadas temporalmente";
//                if (api.getRedis().isRegistred(p.getUniqueId())) {
//                    try (Jedis jedis = api.getRedis().getPool().getResource()) {
//                        return String.valueOf(api.getRedis().getGson().fromJson(jedis.hget("ncore_" + server + "_data", p.getUniqueId().toString()), JsonObject.class).get(finalstat).getAsInt());
//                    }
//                } else {
//                    return "UNKNOW USER";
//                }
            }
        }
        return "UNKNOW STATISTIC OR SERVER";
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
