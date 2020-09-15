package com.github.beelzebu.matrix.bungee.command;

import com.github.beelzebu.matrix.api.MatrixBungeeBootstrap;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.messaging.message.StaffChatMessage;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class HelpOpCommand extends Command {

    private final MatrixBungeeBootstrap bootstrap;
    private final Map<UUID, Long> timer = new HashMap<>();

    public HelpOpCommand(MatrixBungeeBootstrap bootstrap) {
        super("helpop");
        this.bootstrap = bootstrap;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        bootstrap.getApi().getPlugin().runAsync(() -> {
            if (!(sender instanceof ProxiedPlayer)) {
                return;
            }
            ProxiedPlayer pp = (ProxiedPlayer) sender;
            UUID uniqueId = pp.getUniqueId();
            MatrixPlayer matrixPlayer = bootstrap.getApi().getPlayer(uniqueId);
            if (args.length == 1 && args[0].length() <= 2) {
                sender.sendMessage(TextComponent.fromLegacyText(I18n.tl(Message.HELPOP_HELP_USAGE, matrixPlayer.getLastLocale())));
                return;
            }
            if (timer.containsKey(uniqueId) && timer.get(uniqueId) > System.currentTimeMillis()) {
                sender.sendMessage(TextComponent.fromLegacyText(I18n.tl(Message.HELPOP_COOLDOWN, matrixPlayer.getLastLocale()).replace("%cooldown%", String.valueOf((timer.get(uniqueId) - System.currentTimeMillis()) / 1000))));
                return;
            }
            StringBuilder message = new StringBuilder();
            for (String arg : args) {
                message.append(arg).append(" ");
            }
            if (!timer.containsKey(uniqueId) || timer.get(uniqueId) <= System.currentTimeMillis()) {
                String helpopMessageFormatted = I18n.tl(Message.HELPOP_FORMAT, matrixPlayer.getLastLocale()).replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName()).replace("%player_name%", matrixPlayer.getDisplayName()).replace("%message%", message);
                new StaffChatMessage("matrix.helpop.read", helpopMessageFormatted).send();
                sender.sendMessage(TextComponent.fromLegacyText(helpopMessageFormatted));
                if (!sender.hasPermission("matrix.helper")) {
                    timer.put(uniqueId, System.currentTimeMillis() + 30000);
                }
            }
        });
    }
}
