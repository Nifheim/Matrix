package net.nifheim.matrix.auth.paper.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.nifheim.bukkit.commandlib.RegistrableCommand;
import net.nifheim.matrix.api.MatrixProvider;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.player.PlayerManager;
import net.nifheim.matrix.auth.paper.database.AuthDatabase;
import net.nifheim.matrix.auth.paper.exception.UserNotRegisteredException;
import net.nifheim.matrix.auth.paper.security.HashedPassword;
import net.nifheim.matrix.auth.paper.security.PasswordEncryption;
import net.nifheim.matrix.common.messaging.MessagingService;
import net.nifheim.matrix.common.messaging.message.LoginMessage;
import net.nifheim.matrix.common.messaging.rabbitmq.LoginProducer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

public class LoginCommand extends RegistrableCommand {

    private final PlayerManager playerManager = MatrixProvider.getAPI().getPlayerManager();
    private final AuthDatabase authDatabase;
    private final Logger logger;

    public LoginCommand(Plugin plugin, AuthDatabase authDatabase, Logger logger) {
        super(plugin, "login", null, true, "l", "log");
        this.authDatabase = authDatabase;
        this.logger = logger;
    }

    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        // login command, user must pass his password as args[0], when the command is used without args, send information about the usage
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("You must be a player", NamedTextColor.RED));
            return;
        }
        if (args.length == 0) {
            player.sendMessage(Component.translatable("matrix:auth:login.usage", command()));
            return;
        }
        String password = args[0];
        try {
            HashedPassword hashedPassword = authDatabase.getPasswordHash(player.getUniqueId());
            logger.info("hashedPassword: {} password: {}", hashedPassword, password);
            if (PasswordEncryption.comparePassword(password, hashedPassword)) {
                MatrixPlayer matrixPlayer = playerManager.getPlayerSync(player.getUniqueId());
                if (matrixPlayer == null) {
                    player.kick(Component.text("Error validating your session"));
                    return;
                }
                try {
                    MessagingService messagingService = MatrixProvider.getAPI().getService(MessagingService.class);
                    messagingService.getProducer(LoginProducer.class).sendMessage(new LoginMessage(player.getUniqueId(), player.getName()));
                } catch (Exception e) {
                    logger.error("Error sending login message", e);
                }
                player.sendMessage(Component.translatable("login.success", TextColor.color(0x6FE331)));
            } else {
                player.sendMessage(Component.translatable("login.error.password", NamedTextColor.RED));
            }
        } catch (UserNotRegisteredException e) {
            player.sendMessage(Component.translatable("error.unregistered", NamedTextColor.RED));
            logger.error("Error logging in player", e);
        } catch (Exception e) {
            player.sendMessage(Component.translatable("error.unknown", NamedTextColor.RED));
            logger.error("Error logging in player", e);
        }
    }

    public static Component command() {
        return MiniMessage.miniMessage().deserialize("<b><color:#6FE331>/login <password></color></b>");
    }
}
