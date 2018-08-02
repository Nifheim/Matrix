package io.github.beelzebu.matrix.player;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.PlayerOptionType;
import io.github.beelzebu.matrix.api.server.ServerType;
import io.github.beelzebu.matrix.utils.PermsUtils;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Beelzebu
 */
public class BukkitMatrixPlayer extends MongoMatrixPlayer {

    private transient Player player;

    public BukkitMatrixPlayer(UUID uniqueId) {
        this.uniqueId = uniqueId;
        try {
            player = Bukkit.getPlayer(uniqueId);
        } catch (NullPointerException ignore) { // el jugador aún no está online
        }
    }

    public Player getPlayer() {
        return player == null ? player = Bukkit.getPlayer(uniqueId) : player;
    }

    @Override
    public void setOption(PlayerOptionType option, boolean status) {
        if (getPlayer() != null) { // es necesario que esté online para editar esto
            switch (option) {
                case FLY:
                    if (!Matrix.getAPI().getServerInfo().getServerType().toString().contains("MINIGAME")) { // no se debe poner fly si están en juego
                        getPlayer().setAllowFlight(status);
                        getPlayer().setFlying(status);
                    }
                    break;
                case SPEED:
                    if (Matrix.getAPI().getServerInfo().getServerType().equals(ServerType.LOBBY)) { // no hay que poner speed si no es un lobby
                        if (status) {
                            getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, Integer.MAX_VALUE, true, false));
                        } else {
                            getPlayer().removePotionEffect(PotionEffectType.SPEED);
                        }
                    }
                    break;
                case NICKNAME: // TODO: hacer sistema para ocultar staffs y youtubers mientras juegan
                    break;
            }
        }
        super.setOption(option, status);
    }

    @Override
    public void setDisplayname(String displayname) {
        super.setDisplayname(displayname);
        getPlayer().setDisplayName(PermsUtils.getPrefix(uniqueId) + getDisplayname());
    }

    @Override
    public boolean hasPermission(String permission) {
        return getPlayer().hasPermission(permission);
    }
}
