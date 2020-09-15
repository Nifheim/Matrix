package com.github.beelzebu.matrix.util;

import com.github.beelzebu.matrix.api.Matrix;
import java.util.UUID;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;

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
        User user = permsAPI.getUserManager().loadUser(uniqueId).join();
        if (user != null) {
            try {
                String prefix = user.getCachedData().getMetaData().getPrefix();
                if (prefix != null) {
                    return ChatColor.translateAlternateColorCodes('&', prefix);
                }
            } finally {
                if (!Matrix.getAPI().getPlugin().isOnline(uniqueId, true)) {
                    permsAPI.getUserManager().cleanupUser(user);
                }
            }
        }
        return "none";
    }
}
