package io.github.beelzebu.matrix.util;

import io.github.beelzebu.matrix.api.Matrix;
import java.util.UUID;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.MetaData;

public final class PermsUtils {

    private static final LuckPermsApi permsAPI = LuckPerms.getApi();

    public PermsUtils() {
    }

    /**
     * Get the prefix for a player from the permissions matrixPlugin.
     *
     * @param player The UUID of the player to lookup in the permissions matrixPlugin.
     * @return String representing the prefix.
     */
    public static String getPrefix(UUID player) {
        if (Matrix.getAPI().getPlayer(player).getDisplayName().contains(" ")) {
            return Matrix.getAPI().getPlayer(player).getDisplayName().split(" ")[0];
        }
        try {
            User user = permsAPI.getUserSafe(player).orElse(null);
            if (user == null) {
                permsAPI.getStorage().loadUser(player).join();
            }
            if (user != null) {
                Contexts contexts = permsAPI.getContextForUser(user).orElse(null);
                if (contexts != null) {
                    MetaData meta = user.getCachedData().getMetaData(contexts);
                    String prefix = meta.getPrefix();
                    if (prefix != null) {
                        return prefix.replaceAll("&", "§");
                    }
                }
                permsAPI.cleanupUser(user);
            }
        } catch (Exception ignore) {
        }
        return "none";
    }

    public static LuckPermsApi getPermsAPI() {
        return PermsUtils.permsAPI;
    }
}
