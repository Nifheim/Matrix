package com.github.beelzebu.matrix.command;

import com.github.beelzebu.matrix.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.api.MatrixBungeeAPI;
import com.github.beelzebu.matrix.api.config.MatrixConfig;
import com.github.beelzebu.matrix.channels.Channel;
import com.github.beelzebu.matrix.motd.MotdManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Beelzebu
 */
public class MatrixBungeeReload extends Command {

    private final MatrixBungeeAPI api;

    public MatrixBungeeReload(MatrixBungeeAPI api) {
        super("bmatrixreload", "matrix.command.reload");
        this.api = api;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        api.getConfig().reload();
        MatrixBungeeBootstrap.CHANNELS.clear();
        MatrixConfig config = api.getConfig();
        config.getKeys("Channels").forEach((channel) -> MatrixBungeeBootstrap.CHANNELS.put(channel, new Channel(channel, channel, config.getString("Channels." + channel + ".Permission"), ChatColor.valueOf(config.getString("Channels." + channel + ".Color"))).register()));
        MotdManager.onEnable();
    }
}
