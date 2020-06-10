package com.github.beelzebu.matrix.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Beelzebu
 */
public class CountdownCommand extends Command {

    public CountdownCommand() {
        super("addcd", "matrix.admin");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&cUsa &f/countdown <id> <minutos>")));
            return;
        }
        /*
        try (Jedis jedis = RedisManager.getInstance().getPool().getResource()) {
            Countdown c = new Countdown(args[0], System.currentTimeMillis(), System.currentTimeMillis() + (Long.parseLong(args[1]) * 60000));
            jedis.set("countdown:" + c.getId(), Matrix.GSON.toJson(c));
        }
         */
    }
}
