import io.github.beelzebu.matrix.MatrixAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

*This program is distributed in the hope that it will be useful,but WITHOUT
        *
        package io.github.beelzebu.matrix.utils.placeholders;
        */

public class Placeholders {

    public static String rep(Player player, String message) {
        return MatrixAPI.getInstance().rep(PlaceholderAPI.setPlaceholders(player, message));
    }
}
