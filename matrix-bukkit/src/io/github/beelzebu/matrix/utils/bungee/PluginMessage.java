package io.github.beelzebu.matrix.utils.bungee;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.beelzebu.matrix.MatrixBukkitBootstrap;
import io.github.beelzebu.matrix.api.Matrix;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class PluginMessage implements PluginMessageListener {

    private static PluginMessage instance;

    public PluginMessage(MatrixBukkitBootstrap matrixBukkitBootstrap) {
        instance = this;
        Bukkit.getMessenger().registerOutgoingPluginChannel(matrixBukkitBootstrap, "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(matrixBukkitBootstrap, "BungeeCord", instance);
        Bukkit.getMessenger().registerOutgoingPluginChannel(matrixBukkitBootstrap, "RedisBungee");
        Bukkit.getMessenger().registerIncomingPluginChannel(matrixBukkitBootstrap, "RedisBungee", instance);
    }

    public static PluginMessage get() {
        if (instance == null) {
            instance = new PluginMessage(MatrixBukkitBootstrap.getPlugin(MatrixBukkitBootstrap.class));
        }
        return instance;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("RedisBungee")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        if (subChannel.equals("PlayerCount")) {
            String server = in.readUTF();
            int online = in.readInt();
            BungeeServerInfo serverInfo = BungeeServerTracker.getOrCreateServerInfo(server);
            serverInfo.setOnlinePlayers(online);
        }
    }

    public void askPlayerCount(String server) {
        sendMessage("RedisBungee", "PlayerCount", Collections.singletonList(server));
    }

    public void sendMessage(String channel, String subChannel, List<String> message) {
        sendMessage(channel, subChannel, message, Iterables.getFirst(Bukkit.getOnlinePlayers(), null));
    }

    public void sendMessage(String channel, String subChannel, List<String> message, Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(subChannel);
        message.forEach(out::writeUTF);
        if (player != null) {
            try {
                player.sendPluginMessage(MatrixBukkitBootstrap.getPlugin(MatrixBukkitBootstrap.class), channel, out.toByteArray());
            } catch (Exception ex) {
                Matrix.getLogger().log("Hey, you need to install the matrixPlugin in BungeeCord if you have bungeecord enabled in spigot.yml!");
            }
        }
    }
}
