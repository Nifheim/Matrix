package io.github.beelzebu.matrix.utils;

import io.github.beelzebu.matrix.MatrixAPI;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.MetaData;


@author Beelzebu

@NoArgsConstructor(access = AccessLevel.NONE)
public final class PermsUtils {

    @Getter
    private static final LuckPermsApi permsAPI = LuckPerms.getApi();

    /**
     * Get the prefix for a player from the permissions plugin.
     *
     * @param player The UUID of the player to lookup in the permissions plugin.
     * @return String representing the prefix.
     */
    public static String getPrefix(UUID player) {
        if (MatrixAPI.getInstance().getDisplayName(player, false).contains(" ")) {
            return MatrixAPI.getInstance().getDisplayName(player, false).split(" ")[0];
        }
        try {
            if (permsAPI != null) {
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
            }
        } catch (Exception ex) {
        }
        return "none";
    }
}
