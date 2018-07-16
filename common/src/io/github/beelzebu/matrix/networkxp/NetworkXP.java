package io.github.beelzebu.matrix.networkxp;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import java.util.UUID;

public class NetworkXP {

    private static final MatrixAPI api = Matrix.getAPI();

    public static long getXPForPlayer(UUID uuid) {
        if (api.getDatabase().isRegistered(uuid)) {
            return api.getPlayer(uuid).getExp();
        }
        return 0;
    }

    public static int getLevelForPlayer(UUID name) {
        return name != null ? getLevelForXP(getXPForPlayer(name)) : 0;
    }

    public static void addXPForPlayer(UUID name, long exp) {
        setXPForPlayer(name, (getXPForPlayer(name)) + exp);
    }

    public static void setXPForPlayer(UUID uuid, long exp) {
        if (api.getDatabase().isRegistered(uuid)) {
            api.getPlayer(uuid).setExp(exp);
            long oldxp = getXPForPlayer(uuid);
            if (getLevelForXP(exp) > getLevelForXP(oldxp)) {
                api.getPlugin().callLevelUPEvent(uuid, exp, oldxp);
            }
        }
    }

    public static long getXPForLevel(int lvl) {
        if (lvl <= 16 && lvl >= 0) {
            return ((long) (Math.pow(lvl, 2) + 6 * lvl)) * 30;
        } else if (lvl >= 16 && lvl <= 31) {
            return ((long) (2.5 * Math.pow(lvl, 2) - 40.5 * lvl + 360)) * 30;
        } else if (lvl >= 31) {
            return ((long) (4.5 * Math.pow(lvl, 2) - 162.5 * lvl + 2220)) * 30;
        }
        return 0;
    }

    public static int getLevelForXP(long xp) {
        xp /= 30;
        if (xp <= 352 && xp > 0) {
            return (int) (Math.sqrt(xp + 9) - 3);
        } else if (xp >= 394 && xp <= 1507) {
            return (int) ((Math.sqrt(40 * xp - 7839) + 81) * 0.1);
        } else if (xp >= 1628) {
            return (int) ((Math.sqrt(72 * xp - 54215) + 325) / 18);
        }
        return 0;
    }

    public static class MCEXP {

        public static long getXPForPlayer(String name) {
            if (api.getDatabase().isRegistered(api.getPlugin().getUUID(name))) {
                return api.getPlayer(name).getExp() / 30;
            }
            return 0;
        }

        public static long getXPForLevel(int lvl) {
            if (lvl <= 16) {
                return (long) (Math.pow(lvl, 2) + 6 * lvl);
            } else if (lvl >= 16 && lvl <= 31) {
                return (long) (2.5 * Math.pow(lvl, 2) - 40.5 * lvl + 360);
            } else if (lvl >= 31) {
                return (long) (4.5 * Math.pow(lvl, 2) - 162.5 * lvl + 2220);
            }
            return 0;
        }
    }
}
