package com.github.beelzebu.matrix.bukkit.util;

import com.github.beelzebu.matrix.api.MatrixBukkitBootstrap;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Jaime Suárez
 */
public class BungeeUtil {

    public static void move(@NotNull Player player, @NotNull String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(MatrixBukkitBootstrap.getPlugin(MatrixBukkitBootstrap.class), "BungeeCord", out.toByteArray());
    }

    public static void move(@NotNull Player sender, @NotNull String name, @NotNull String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ConnectOther");
        out.writeUTF(name);
        out.writeUTF(server);
        sender.sendPluginMessage(MatrixBukkitBootstrap.getPlugin(MatrixBukkitBootstrap.class), "BungeeCord", out.toByteArray());
    }
}
