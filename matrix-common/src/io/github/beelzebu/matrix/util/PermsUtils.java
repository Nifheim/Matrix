package io.github.beelzebu.matrix.util;

import io.github.beelzebu.matrix.api.Matrix;
import java.util.UUID;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

public final class PermsUtils {

    private static final LuckPerms permsAPI = LuckPermsProvider.get();

    public PermsUtils() {
    }

    /**
     * Get the prefix for a player from the permissions matrixPlugin.
     *
     * @param uniqueId The UUID of the player to lookup in the permissions matrixPlugin.
     * @return String representing the prefix.
     */
    public static String getPrefix(UUID uniqueId) {
        if (Matrix.getAPI().getPlayer(uniqueId).getDisplayName().contains(" ")) {
            return Matrix.getAPI().getPlayer(uniqueId).getDisplayName().split(" ")[0];
        }
        User user = permsAPI.getUserManager().getUser(uniqueId);
        try {
            if (user == null) {
                user = permsAPI.getUserManager().loadUser(uniqueId).join();
            }
            if (user != null) {
                String prefix = user.getCachedData().getMetaData().getPrefix();
                if (prefix != null) {
                    return prefix.replaceAll("&", "§");
                }
            }
        } catch (Exception ignore) {
        } finally {
            permsAPI.getUserManager().cleanupUser(user);
        }
        return "none";
    }
}
