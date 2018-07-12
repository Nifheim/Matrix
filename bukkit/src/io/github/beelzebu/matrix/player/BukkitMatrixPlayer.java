package io.github.beelzebu.matrix.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.beelzebu.matrix.player.options.PlayerOptionType;
import io.github.beelzebu.matrix.utils.PermsUtils;
import io.github.beelzebu.matrix.utils.ServerType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import redis.clients.jedis.Jedis;

/**
 * @author Beelzebu
 */
public class BukkitMatrixPlayer extends MatrixPlayer {

    public final Map<PlayerOptionType, Boolean> data = new HashMap<>();
    private Player player;

    public BukkitMatrixPlayer(UUID uniqueId) {
        super(uniqueId);
    }

    @Override
    public String getName() {
        return getPlayer().getName();
    }

    @Override
    public boolean getOption(PlayerOptionType option) {
        if (data.containsKey(option)) {
            return data.get(option);
        }
        try (Jedis jedis = core.getRedis().getPool().getResource()) {
            JsonArray options;
            try {
                options = core.getGson().fromJson(jedis.hget("ncore_data", uniqueId.toString()), JsonObject.class).get("options").getAsJsonArray();
            } catch (NullPointerException ex) {
                options = new JsonArray();
            }
            options.forEach(je -> {
                data.put(PlayerOptionType.valueOf(je.getAsString()), true);
            });
        }
        if (data.containsKey(option)) {
            return data.get(option);
        }
        return false;
    }

    @Override
    public void setOption(PlayerOptionType option, boolean status) {
        if (core.getRedis().isRegistred(uniqueId)) {
            core.getRedis().setData(uniqueId, "options", getActiveOptions());
        }
        switch (option) {
            case FLY:
                if (!getPlayer().hasPermission("matrix.vip.sir") && !getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                    status = false;
                }
                getPlayer().setAllowFlight(status);
                getPlayer().setFlying(status);
                break;
            case SPEED:
                if (!isInLobby()) {
                    status = false;
                }
                if (status) {
                    getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, true, false));
                } else {
                    getPlayer().removePotionEffect(PotionEffectType.SPEED);
                }
                break;
            case NICKNAME:
                break;
        }
        data.put(option, status);
    }

    @Override
    public void setAllOptions(Map<PlayerOptionType, Boolean> options) {
        data.clear();
        options.forEach((key, value) -> setOption(key, value));
    }

    @Override
    public Set<PlayerOptionType> getActiveOptions() {
        Set<PlayerOptionType> options = new HashSet<>();
        data.forEach((key, value) -> {
            if (value) {
                options.add(key);
            }
        });
        return options;
    }

    @Override
    public boolean isInLobby() {
        return core.getServerInfo().getServerType().equals(ServerType.LOBBY) || (core.getServerInfo().getServerType().equals(ServerType.MINIGAME_MULTIARENA) && getPlayer().getWorld().getName().equals(core.getConfig().getString("Lobby World", "Spawn")));
    }

    @Override
    public void setNick(String nick) {
        setNick(nick, true);
    }

    public void setNick(String nick, boolean withprefix) {
        if (nick == null || nick.equals("")) {
            nick = getPlayer().getName();
        }
        core.getRedis().setData(uniqueId, "displayname", nick);
        getPlayer().setDisplayName(withprefix ? PermsUtils.getPrefix(uniqueId) + nick : nick);
    }

    @Override
    public String getIP() {
        try {
            return getPlayer().getAddress().getAddress().getHostAddress();
        } catch (NullPointerException ex) {
            return "0.0.0.0";
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        return getPlayer().hasPermission(permission);
    }

    public Player getPlayer() {
        return player == null ? player = Bukkit.getPlayer(uniqueId) : player;
    }
}
