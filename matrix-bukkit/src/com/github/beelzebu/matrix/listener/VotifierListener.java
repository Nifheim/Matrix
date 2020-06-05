package com.github.beelzebu.matrix.listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import com.github.beelzebu.matrix.MatrixBukkitBootstrap;
import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.util.ReadURL;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VotifierListener implements Listener {

    private final MatrixBukkitBootstrap plugin;

    public VotifierListener(MatrixBukkitBootstrap matrixBukkitBootstrap) {
        plugin = matrixBukkitBootstrap;
    }

    @EventHandler
    public void onVotifier(VotifierEvent e) {
        Vote vote = e.getVote();
        if (vote.getServiceName().contains("40")) {
            Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    ReadURL.read("http://40servidoresmc.es/api2.php?nombre=" + vote.getUsername() + "&clave=" + plugin.getConfig().getString("clave"));
                } catch (Exception ex) {
                    Matrix.getLogger().info("Can't send the vote for " + vote.getUsername());
                }
            });
        }
    }
}
