package io.github.beelzebu.matrix.command;

import io.github.beelzebu.matrix.api.Matrix;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import redis.clients.jedis.Jedis;

/**
 * @author Beelzebu
 */
public class RanksAsdCommand extends Command {

    public RanksAsdCommand() {
        super("ranksasd");
    }

    @Override
    public void execute(CommandSender sender, String[] strings) {
        try (Jedis jedis = Matrix.getAPI().getRedis().getPool().getResource()) {
            jedis.hgetAll("discord:verified").forEach((id, name) -> ProxyServer.getInstance().getPluginManager().dispatchCommand(sender, "lpb user " + name + " parent add elite 7d"));
        }
    }
}
