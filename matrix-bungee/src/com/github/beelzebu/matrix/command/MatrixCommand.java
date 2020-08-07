package com.github.beelzebu.matrix.command;

import com.github.beelzebu.matrix.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.api.config.MatrixConfig;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.channels.Channel;
import com.github.beelzebu.matrix.motd.MotdManager;
import java.util.Optional;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author Beelzebu
 */
public class MatrixCommand extends Command {

    private final MatrixBungeeBootstrap bungeeBootstrap;

    public MatrixCommand(MatrixBungeeBootstrap bungeeBootstrap) {
        super("bmatrix", "matrix.command.reload", "bungeematrix");
        this.bungeeBootstrap = bungeeBootstrap;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length >= 1) {
            switch (args[0]) {
                case "reload":
                    MatrixBungeeBootstrap.CHANNELS.values().forEach(Channel::unregister);
                    MatrixBungeeBootstrap.CHANNELS.clear();
                    MatrixConfig config = bungeeBootstrap.getConfig();
                    config.getKeys("Channels").forEach((channel) -> MatrixBungeeBootstrap.CHANNELS.put(channel, new Channel(channel, channel, config.getString("Channels." + channel + ".Permission"), ChatColor.valueOf(config.getString("Channels." + channel + ".Color"))).register()));
                    MotdManager.onEnable();
                    bungeeBootstrap.getInfluencerManager().reloadInfluencers();
                    bungeeBootstrap.getApi().reload();
                    break;
                case "purge":
                    if (args.length == 3) {
                        switch (args[1]) {
                            case "field":
                                bungeeBootstrap.getApi().getCache().purgeForAllPlayers(args[2]);
                                bungeeBootstrap.getApi().getDatabase().purgeForAllPlayers(args[2]);
                                sender.sendMessage(TextComponent.fromLegacyText(StringUtils.replace("&7Successfully removed &6" + args[2] + "&7 field from all players.")));
                                break;
                            case "player":
                                Optional<MatrixPlayer> playerOptional = bungeeBootstrap.getApi().getCache().getPlayer(args[2]);
                                if (playerOptional.isPresent()) {
                                    MatrixPlayer matrixPlayer = playerOptional.get();
                                    bungeeBootstrap.getApi().getCache().removePlayer(matrixPlayer);
                                    sender.sendMessage(TextComponent.fromLegacyText(StringUtils.replace("&6" + matrixPlayer.getName() + " &7 successfully removed from cache.")));
                                } else {
                                    sender.sendMessage(TextComponent.fromLegacyText(StringUtils.replace("&6" + args[2] + "&7 is not cached.")));
                                }
                                break;
                        }
                    }
            }
        }

    }
}
