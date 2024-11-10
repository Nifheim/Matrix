package net.nifheim.matrix.auth.paper;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import net.nifheim.bukkit.commandlib.CommandAPI;
import net.nifheim.matrix.auth.paper.command.LoginCommand;
import net.nifheim.matrix.auth.paper.command.RegisterCommand;
import net.nifheim.matrix.auth.paper.database.AuthDatabase;
import net.nifheim.matrix.auth.paper.task.MessageSenderTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MatrixAuth extends JavaPlugin {

    private final TranslationRegistry translationRegistry = TranslationRegistry.create(Key.key("matrix:auth"));

    @Override
    public void onEnable() {
        try {
            copyMessagesFiles();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        registerMessages();
        translationRegistry.defaultLocale(Locale.forLanguageTag("es"));
        GlobalTranslator.translator().addSource(translationRegistry);
        AuthDatabase database = new AuthDatabase(getSLF4JLogger());
        CommandAPI.registerCommand(this, new LoginCommand(this, database, getSLF4JLogger()));
        CommandAPI.registerCommand(this, new RegisterCommand(this, database, getSLF4JLogger()));
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new MessageSenderTask(), 0, 100);
    }

    @Override
    public void onDisable() {
        if (!Bukkit.isStopping()) {
            getSLF4JLogger().error("Server was not shutting down but plugin is disabling, shutting down server.");
            Bukkit.shutdown();
        }
    }

    private static final String MESSAGES = "messages";
    private static final String PROPERTY_FILE_EXTENSION = ".properties";
    private static final String SPANISH_PROPERTY_FILE = "_es" + PROPERTY_FILE_EXTENSION;

    private void copyMessagesFiles() throws IOException {
        Path messagesFolderPath = getMessagesFolderPath();
        createDirIfNotExists(messagesFolderPath);
        copyFileIfNotExists(messagesFolderPath, MESSAGES + PROPERTY_FILE_EXTENSION);
        copyFileIfNotExists(messagesFolderPath, MESSAGES + SPANISH_PROPERTY_FILE);
    }

    private Path getMessagesFolderPath() {
        return getDataFolder().toPath().resolve(MESSAGES);
    }

    private void createDirIfNotExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    private void copyFileIfNotExists(Path parentPath, String fileName) throws IOException {
        Path filePath = parentPath.resolve(fileName);
        if (!Files.exists(filePath)) {
            try (Reader reader = new InputStreamReader(Objects.requireNonNull(getClassLoader().getResourceAsStream(fileName)), StandardCharsets.UTF_8);
                 Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                char[] buffer = new char[1024];
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, read);
                }
            }
        }
    }

    /**
     * Load message files as resource bundles from the data folder
     */
    private void registerMessages() {
        File messagesFolder = new File(getDataFolder(), "messages");
        File[] messageFiles = messagesFolder.listFiles();
        if (messageFiles != null) {
            for (File file : messageFiles) {
                if (file.isFile()) {
                    Locale locale = getLocale(file);
                    ResourceBundle bundle = ResourceBundle.getBundle("messages", locale, UTF8ResourceBundleControl.get());
                    translationRegistry.registerAll(locale, bundle, false);
                    getSLF4JLogger().info("Registered messages for {}", locale);
                }
            }
        }
    }

    /**
     * Get locale from file name. If there's no provided locale in the file name, it assumes English.
     */
    private Locale getLocale(File file) {
        String fileName = file.getName();
        String delimiter = "_";
        String extensionPoint = ".";
        int startIndex = fileName.lastIndexOf(delimiter) + 1;
        int endIndex = fileName.lastIndexOf(extensionPoint);

        // If startIndex and endIndex point to the same position, there's no locale
        if (startIndex == endIndex) {
            return Locale.ENGLISH;
        }

        String localeString = fileName.substring(startIndex, endIndex);
        String languageTag = localeString.replace("_", "-");  // replace "_" with "-" for BCP 47 compliance
        Locale locale = Locale.forLanguageTag(languageTag);

        // If the formed locale is empty, return English as default
        if ("".equals(locale.getLanguage())) {
            return Locale.ENGLISH;
        }

        return locale;
    }
}
