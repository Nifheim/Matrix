package com.github.beelzebu.matrix.util.placeholders;

import cl.indiopikaro.jmatrix.api.util.StringUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class Placeholders {

    public static String rep(Player player, String message) {
        return StringUtils.replace(PlaceholderAPI.setPlaceholders(player, message));
    }
}
