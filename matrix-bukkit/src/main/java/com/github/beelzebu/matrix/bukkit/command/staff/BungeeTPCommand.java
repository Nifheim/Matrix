package com.github.beelzebu.matrix.bukkit.command.staff;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.command.MatrixCommand;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.bukkit.util.BungeeUtil;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public class BungeeTPCommand extends MatrixCommand {

    public BungeeTPCommand() {
        super("btp", "matrix.command.btp", "bungeetp");
    }

    @Override
    public void onCommand(CommandSender sender, String @NotNull [] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 1) {
                player.sendMessage(StringUtils.replace("&aSearching server with name: " + args[0]));
                Matrix.getAPI().getServerManager().getServer(args[0]).thenAccept(optionalServerInfo -> {
                    if (optionalServerInfo.isPresent()) {
                        BungeeUtil.move(player, optionalServerInfo.get().getServerName());
                    } else {
                        player.sendMessage(TextComponent.fromLegacyText(StringUtils.replace("&cThere is no server with the specified name.")));
                    }
                });
            } else if (args.length == 2) {
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(StringUtils.replace("&cThere is no player named " + args[0]));
                    return;
                }
                player.sendMessage(StringUtils.replace("&aSearching server with name: " + args[1]));
                Matrix.getAPI().getServerManager().getServer(args[1]).thenAccept(optionalServerInfo -> {
                    if (optionalServerInfo.isPresent()) {
                        BungeeUtil.move(target, optionalServerInfo.get().getServerName());
                    } else {
                        player.sendMessage(TextComponent.fromLegacyText(StringUtils.replace("&cThere is no server with the specified name.")));
                    }
                });
            }
        } else {
            sender.sendMessage(TextComponent.fromLegacyText(I18n.tl(Message.GENERAL_NO_CONSOLE, I18n.DEFAULT_LOCALE)));
        }
    }
}
