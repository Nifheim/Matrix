package net.nifheim.matrix.auth.paper.command;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.nifheim.bukkit.commandlib.RegistrableCommand;
import net.nifheim.matrix.api.MatrixProvider;
import net.nifheim.matrix.api.player.MatrixPlayer;
import net.nifheim.matrix.api.player.PlayerManager;
import net.nifheim.matrix.auth.paper.database.AuthDatabase;
import net.nifheim.matrix.auth.paper.exception.InvalidPasswordException;
import net.nifheim.matrix.auth.paper.security.PasswordEncryption;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class RegisterCommand extends RegistrableCommand {

    private static final Set<String> BLACKLIST = Set.of("help", "password", "contraseña", "123456", "asdasd", "ayuda");
    private final BiConsumer<CommandSender, InvalidPasswordException> exceptionHandler = (sender, e) -> {
        TranslatableComponent error = Component.translatable("register.error.password");
        switch (e.getReason()) {
            case LENGTH -> sender.sendMessage(error.arguments(Component.translatable("register.error.invalid.length")));
            case BLACKLISTED ->
                    sender.sendMessage(error.arguments(Component.translatable("register.error.invalid.blacklist")));
            default -> sender.sendMessage(error);
        }
    };
    private final PlayerManager playerManager = MatrixProvider.getAPI().getPlayerManager();
    private final AuthDatabase authDatabase;
    private final Logger logger;

    public RegisterCommand(Plugin plugin, AuthDatabase authDatabase, Logger logger) {
        super(plugin, "register", null, true, "reg");
        this.authDatabase = authDatabase;
        this.logger = logger;
    }

    @Override
    public void onCommand(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            handleNonPlayerRegistration(sender, args);
        } else {
            handlePlayerRegistration(sender, args, player);
        }
    }

    private void handleNonPlayerRegistration(CommandSender sender, String[] args) {
        if (args.length == 2) {
            String name = args[0];
            String password = args[1];
            performRegistration(sender, name, password, () -> {
                try {
                    authDatabase.insertHashedPassword(name, PasswordEncryption.computeHash(password));
                    sender.sendMessage(Component.translatable("register.success", TextColor.color(0x6FE331)));
                } catch (Exception e) {
                    sender.sendMessage(Component.translatable("error.unknown", NamedTextColor.RED));
                    logger.error("Error registering player from console", e);
                }
            });
        } else {
            sender.sendMessage(Component.translatable("register.usage", command()));
        }
    }

    private void handlePlayerRegistration(CommandSender sender, String[] args, Player player) {
        if (args.length == 2) {
            String password = args[0];
            String passwordConfirmation = args[1];
            MatrixPlayer matrixPlayer = playerManager.getPlayerSync(player.getUniqueId());
            Objects.requireNonNull(matrixPlayer, "matrixPlayer");
            if (validPlayerRegistrationConditions(player, password, passwordConfirmation, matrixPlayer)) {
                performRegistration(sender, player.getUniqueId(), password, () -> {
                    try {
                        authDatabase.insertHashedPassword(player.getUniqueId(), PasswordEncryption.computeHash(password));
                        matrixPlayer.setRegistered(true);
                        matrixPlayer.setLoggedIn(true);
                        sender.sendMessage(Component.translatable("register.success", TextColor.color(0x6FE331)));
                    } catch (Exception e) {
                        sender.sendMessage(Component.translatable("error.unknown"));
                    }
                });
            }
        } else {
            sender.sendMessage(Component.translatable("register.usage", command()));
        }
    }

    private boolean validPlayerRegistrationConditions(Player player, String password, String passwordConfirmation, MatrixPlayer matrixPlayer) {
        if (!Objects.equals(password, passwordConfirmation)) {
            player.sendMessage(Component.translatable("register.error.match", NamedTextColor.RED));
            return false;
        }
        if (matrixPlayer == null) {
            player.kick(Component.text("Error validating your session"));
            return false;
        }
        if (matrixPlayer.isRegistered()) {
            player.sendMessage(Component.translatable("error.registered", NamedTextColor.RED));
            return false;
        }
        return true;
    }

    private void performRegistration(CommandSender sender, String identifier, String password, Runnable action) {
        validateAndPasswordAction(sender, identifier, password, action);
    }

    private void performRegistration(CommandSender sender, UUID uniqueId, String password, Runnable action) {
        validateAndPasswordAction(sender, uniqueId, password, action);
    }

    private void validatePassword(String password) throws InvalidPasswordException {
        if (password == null || password.isBlank() || password.length() < 5) {
            throw new InvalidPasswordException(InvalidPasswordException.InvalidReason.LENGTH);
        }
        if (BLACKLIST.contains(password)) {
            throw new InvalidPasswordException(InvalidPasswordException.InvalidReason.BLACKLISTED);
        }
    }

    private void validateAndPasswordAction(CommandSender sender, Object player, String password, @NotNull Runnable action) {
        try {
            validatePassword(password);
            action.run();
        } catch (InvalidPasswordException e) {
            exceptionHandler.accept(sender, e);
        } catch (Exception e) {
            logger.error("Error registering player " + player.toString(), e);
        }
    }

    public static @NotNull Component command() {
        return MiniMessage.miniMessage().deserialize("<b><color:#6FE331>/register <password> <password></color></b>");
    }
}
