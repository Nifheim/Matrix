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
import org.jetbrains.annotations.NotNull;

public class HelpOpCommand extends Command {

    private final MatrixBungeeBootstrap bootstrap;
    private final Map<UUID, Long> timer = new HashMap<>();

    public HelpOpCommand(MatrixBungeeBootstrap bootstrap) {
        super("helpop");
        this.bootstrap = bootstrap;
    }

    @Override
    public void execute(CommandSender sender, String @NotNull [] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            return;
        }
        ProxiedPlayer proxiedPlayer = (ProxiedPlayer) sender;
        UUID uniqueId = proxiedPlayer.getUniqueId();
        if (args.length <= 1) {
            sender.sendMessage(TextComponent.fromLegacyText(I18n.tl(Message.HELPOP_HELP_USAGE, proxiedPlayer.getLocale().getLanguage().split("_")[0])));
            return;
        }
        bootstrap.getApi().getPlugin().getBootstrap().getScheduler().executeAsync(() -> {
            MatrixPlayer matrixPlayer = bootstrap.getApi().getPlayerManager().getPlayer(uniqueId).join();
            StringBuilder message = new StringBuilder();
            for (String arg : args) {
                message.append(arg).append(" ");
            }
            if (message.length() <= 5) {
                sender.sendMessage(TextComponent.fromLegacyText(I18n.tl(Message.HELPOP_HELP_USAGE, matrixPlayer.getLastLocale())));
                return;
            }
            if (timer.containsKey(uniqueId) && timer.get(uniqueId) > System.currentTimeMillis()) {
                sender.sendMessage(TextComponent.fromLegacyText(I18n.tl(Message.HELPOP_COOLDOWN, matrixPlayer.getLastLocale()).replace("%cooldown%", String.valueOf((timer.get(uniqueId) - System.currentTimeMillis()) / 1000))));
                return;
            }
            if (!timer.containsKey(uniqueId) || timer.get(uniqueId) <= System.currentTimeMillis()) {
                String helpopMessageFormatted = I18n.tl(Message.HELPOP_FORMAT, matrixPlayer.getLastLocale()).replace("%server%", ((ProxiedPlayer) sender).getServer().getInfo().getName()).replace("%player_name%", matrixPlayer.getDisplayName()).replace("%message%", message);
                new StaffChatMessage("matrix.helpop.read", helpopMessageFormatted).send();
                sender.sendMessage(TextComponent.fromLegacyText(helpopMessageFormatted));
                timer.put(uniqueId, System.currentTimeMillis() + 30000);
            }
        });
    }
}
