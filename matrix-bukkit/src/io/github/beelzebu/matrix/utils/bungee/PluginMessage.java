package io.github.beelzebu.matrix.utils.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.beelzebu.matrix.MatrixBukkit;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class PluginMessage implements PluginMessageListener {

    private static PluginMessage instance;

    public PluginMessage(MatrixBukkit matrixBukkit) {
        instance = this;
        Bukkit.getMessenger().registerOutgoingPluginChannel(matrixBukkit, "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(matrixBukkit, "BungeeCord", instance);
        Bukkit.getMessenger().registerOutgoingPluginChannel(matrixBukkit, "RedisBungee");
        Bukkit.getMessenger().registerIncomingPluginChannel(matrixBukkit, "RedisBungee", instance);
    }

    public static PluginMessage get() {
        if (instance == null) {
            instance = new PluginMessage(MatrixBukkit.getPlugin(MatrixBukkit.class));
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
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerCount");
        out.writeUTF(server);

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        if (players.size() > 0) {
            players.iterator().next().sendPluginMessage(MatrixBukkit.getPlugin(MatrixBukkit.class), "RedisBungee", out.toByteArray());
        }
    }
}
