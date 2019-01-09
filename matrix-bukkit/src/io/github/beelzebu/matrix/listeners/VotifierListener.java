package io.github.beelzebu.matrix.listeners;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import io.github.beelzebu.matrix.MatrixBukkitBootstrap;
import io.github.beelzebu.matrix.utils.ReadURL;
import java.util.logging.Level;
import java.util.logging.Logger;
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
                    Logger.getLogger(VotifierListener.class.getName()).log(Level.WARNING, "Can''t send the vote for {0}", vote.getUsername());
                }
            });
        }
    }
}
