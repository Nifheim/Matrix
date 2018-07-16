package io.github.beelzebu.matrix.utils.placeholders;

import io.github.beelzebu.matrix.api.Matrix;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class Placeholders {

    public static String rep(Player player, String message) {
        return Matrix.getAPI().rep(PlaceholderAPI.setPlaceholders(player, message));
    }
}
