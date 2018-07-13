package io.github.beelzebu.matrix.utils.placeholders;

import io.github.beelzebu.matrix.MatrixAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class Placeholders {

    public static String rep(Player player, String message) {
        return MatrixAPI.getInstance().rep(PlaceholderAPI.setPlaceholders(player, message));
    }
}
