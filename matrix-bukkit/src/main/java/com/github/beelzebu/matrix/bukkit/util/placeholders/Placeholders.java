package com.github.beelzebu.matrix.bukkit.util.placeholders;

import com.github.beelzebu.matrix.api.util.StringUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholders {

    public static @NotNull String rep(Player player, @NotNull String message) {
        return StringUtils.replace(PlaceholderAPI.setPlaceholders(player, message));
    }
}
