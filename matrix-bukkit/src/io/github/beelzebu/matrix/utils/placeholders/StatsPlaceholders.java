package io.github.beelzebu.matrix.utils.placeholders;

import io.github.beelzebu.matrix.MatrixBukkitBootstrap;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.networkxp.NetworkXP;
import io.github.beelzebu.matrix.utils.bungee.BungeeServerTracker;
import java.text.DecimalFormat;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class StatsPlaceholders extends EZPlaceholderHook {

    private final MatrixAPI api = Matrix.getAPI();

    public StatsPlaceholders(MatrixBukkitBootstrap matrixBukkitBootstrap) {
        super(matrixBukkitBootstrap, "ncore-stats");
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
            if (stat.matches("^onlineplayers$")) {
                int online = 0;
                online = Bukkit.getOnlinePlayers().stream().filter((op) -> (p.canSee(op))).map((_item) -> 1).reduce(online, Integer::sum);
                return String.valueOf(online);
            }
            if (stat.matches("online_.*")) {
                return String.valueOf(BungeeServerTracker.getPlayersOnline(stat.split("_")[1]));
            }
        }
        if (stat.startsWith("level")) {
            int lvl = NetworkXP.getLevelForPlayer(p.getUniqueId());
            double percent = (((NetworkXP.getXPForPlayer(p.getUniqueId()) - NetworkXP.getXPForLevel(lvl)) * 100) / (NetworkXP.getXPForLevel(lvl + 1) - NetworkXP.getXPForLevel(lvl)));
            if (stat.equals("level")) {
                return String.valueOf(lvl);
            }
            if (stat.equals("levelf")) {
                String prog = "§c";
                while (ChatColor.stripColor(prog).length() < (int) percent / 2) {
                    prog += "|";
                }
                prog += "§7";
                while (ChatColor.stripColor(prog).length() < 50) {
                    prog += "|";
                }
                return prog;
            }
            if (stat.equals("levelp")) {
                return new DecimalFormat("#.#").format(percent);
            }
            if (stat.equals("leveln")) {
                return String.valueOf(NetworkXP.getXPForLevel(lvl + 1) - NetworkXP.getXPForPlayer(p.getUniqueId()));
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
}
