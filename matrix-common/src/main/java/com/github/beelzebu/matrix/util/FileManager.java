package com.github.beelzebu.matrix.util;

import com.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;

public class FileManager {

    private final MatrixPlugin plugin;
    private final @NotNull File messagesFile;
    private final @NotNull File messages_esFile;
    private final @NotNull File configFile;
    private final @NotNull File dataFile;

    public FileManager(@NotNull MatrixPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "MatrixPlugin can't be null.");
        File messagesFolder = new File(plugin.getDataFolder(), "messages");
        if (!messagesFolder.exists()) {
            messagesFolder.mkdirs();
        }
        messagesFile = new File(messagesFolder, "messages_en.yml");
        messages_esFile = new File(messagesFolder, "messages_es.yml");
        configFile = new File(plugin.getDataFolder(), "config.yml");
        dataFile = new File(plugin.getDataFolder(), "data.yml");
    }

    public void copy(@NotNull InputStream in, @NotNull File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (IOException e) {
            Logger.getLogger(FileManager.class.getName()).log(Level.WARNING, "Can''t copy the file {0} to the matrixPlugin data folder. Cause: {1}", new Object[]{file.getName(), e.getCause().toString()});
        }
    }

    public void generateFiles() {
        plugin.getDataFolder().mkdirs();
        File oldMessagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (oldMessagesFile.exists()) {
            oldMessagesFile.renameTo(messagesFile);
        }
        if (!messagesFile.exists()) {
            copy(plugin.getResource("messages_en.yml"), messagesFile);
        }
        if (!messages_esFile.exists()) {
            copy(plugin.getResource("messages_es.yml"), messages_esFile);
        }
        if (!configFile.exists()) {
            copy(plugin.getResource("config.yml"), configFile);
        }
        if (!dataFile.exists()) {
            copy(plugin.getResource("data.yml"), dataFile);
        }
    }

    public void updateMessages() {
    }

    public @NotNull File getMessagesFile() {
        return messagesFile;
    }
}
