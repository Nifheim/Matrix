package io.github.beelzebu.matrix.listener;

import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import io.github.beelzebu.matrix.MatrixBungee;
import io.github.beelzebu.matrix.api.Matrix;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

@RequiredArgsConstructor
public class PubSubMessageListener implements Listener {

    private final MatrixBungee plugin;

    @EventHandler
    public void onMessage(PubSubMessageEvent e) {
        if (e.getChannel().equals("NifheimHelpop") || e.getChannel().equals("Channel") || e.getChannel().equals("Maintenance")) {
            Matrix.getAPI().getPlugin().runAsync(() -> {
                if (e.getChannel().equals("NifheimHelpop")) {
                    ProxyServer.getInstance().getPlayers().stream().filter((pp) -> (pp.hasPermission("matrix.staff.aprendiz"))).forEachOrdered((pp) -> pp.sendMessage(e.getMessage()));
                    ProxyServer.getInstance().getConsole().sendMessage(e.getMessage());
                } else if (e.getChannel().equalsIgnoreCase("Maintenance")) {
                    if (e.getMessage().equals("switch")) {
                        plugin.setMaintenance(!plugin.isMaintenance());
                        if (plugin.isMaintenance()) {
                            ProxyServer.getInstance().getScheduler().schedule(plugin, () -> ProxyServer.getInstance().getPlayers().forEach(proxiedPlayer -> proxiedPlayer.disconnect("")), 500, TimeUnit.MILLISECONDS);
                        }
                    }
                }
            });
        }
    }
}
