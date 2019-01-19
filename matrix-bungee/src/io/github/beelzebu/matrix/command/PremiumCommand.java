package io.github.beelzebu.matrix.command;

import io.github.beelzebu.matrix.MatrixBungeeBootstrap;
import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Beelzebu
 */
public class PremiumCommand extends Command {

    private final Map<String, Long> players = new HashMap<>();

    public PremiumCommand(MatrixBungeeBootstrap plugin) {
        super("premium");
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            Iterator<Map.Entry<String, Long>> it = players.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Long> ent = it.next();
                if (ent.getValue() <= System.currentTimeMillis()) {
                    players.remove(ent.getKey());
                }
            }
        }, 1, 5, TimeUnit.MINUTES);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            if (players.containsKey(sender.getName())) {
                MatrixPlayer mp = Matrix.getAPI().getPlayer(sender.getName());
                mp.setPremium(true);
                players.remove(sender.getName());
            } else {
                players.put(sender.getName(), System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5));
                sender.sendMessage(Matrix.getAPI().rep("&4&lAtención"));
                sender.sendMessage(Matrix.getAPI().rep("&7Este comando borrará todos tus datos ligados a las modalidades, tienes 5 minutos para volver a usarlo y confirmar tu desición."));
            }
        }
    }
}
