package com.github.beelzebu.matrix.bukkit.command.staff;

import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.util.StringUtils;
import com.github.beelzebu.matrix.bukkit.util.BungeeUtil;
import net.md_5.bungee.api.chat.TextComponent;
import net.nifheim.bukkit.util.command.MatrixCommand;
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
            if (args.length == 1) {
                Player player = (Player) sender;
                player.sendMessage(StringUtils.replace("&aSearching server with name: " + args[0]));
                api.getServerManager().getServer(args[0]).thenAccept(optionalServerInfo -> {
                    if (optionalServerInfo.isPresent()) {
                        BungeeUtil.move(player, optionalServerInfo.get().getServerName());
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
