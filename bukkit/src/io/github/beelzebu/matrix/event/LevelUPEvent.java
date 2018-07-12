package io.github.beelzebu.matrix.event;

import io.github.beelzebu.matrix.networkxp.NetworkXP;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class LevelUPEvent extends PlayerEvent {

    private final static HandlerList handlers = new HandlerList();
    private final long newxp;
    private final long oldxp;

    public LevelUPEvent(UUID player, long newxp, long oldxp) {
        super(Bukkit.getPlayer(player));
        this.newxp = newxp;
        this.oldxp = oldxp;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public long getNewXP() {
        return newxp;
    }

    public long getOldXP() {
        return oldxp;
    }

    public int getNewLvl() {
        return NetworkXP.getLevelForPlayer(getPlayer().getUniqueId());
    }

    public int getOldLvl() {
        return NetworkXP.getLevelForXP(oldxp);
    }

    public long getXPForNextLvl() {
        return NetworkXP.getXPForLevel(NetworkXP.getLevelForPlayer(getPlayer().getUniqueId()) + 1) - NetworkXP.getXPForPlayer(getPlayer().getUniqueId());
    }
}
