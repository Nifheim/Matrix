package io.github.beelzebu.matrix.utils;

import com.google.common.base.Charsets;
import io.github.beelzebu.matrix.MatrixAPI;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;


@author Beelzebu

public class FileManager {

    private final MatrixAPI core;
    private final File messagesFile;
    private final File messages_esFile;
    private final File configFile;
    private final File dataFile;

    public FileManager(MatrixAPI u) {
        core = u;
        messagesFile = new File(core.getDataFolder(), "messages.yml");
        messages_esFile = new File(core.getDataFolder(), "messages_es.yml");
        configFile = new File(core.getDataFolder(), "config.yml");
        dataFile = new File(core.getDataFolder(), "data.yml");
    }

    public void copy(InputStream in, File file) {
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
            Logger.getLogger(FileManager.class.getName()).log(Level.WARNING, "Can''t copy the file {0} to the plugin data folder. Cause: {1}", new Object[]{file.getName(), e.getCause().toString()});
        }
    }

    public void generateFiles() {
        core.getDataFolder().mkdirs();
        if (!messagesFile.exists()) {
            copy(core.getMethods().getResource("messages.yml"), messagesFile);
        }
        if (!messages_esFile.exists()) {
            copy(core.getMethods().getResource("messages_es.yml"), messages_esFile);
        }
        if (!configFile.exists()) {
            copy(core.getMethods().getResource("config.yml"), configFile);
        }
        if (!dataFile.exists()) {
            copy(core.getMethods().getResource("data.yml"), dataFile);
        }
    }

    public void updateMessages() {
        try {
            List<String> lines = FileUtils.readLines(messagesFile, Charsets.UTF_8);
            int index;
            if (lines.contains("ChatControl:")) {
                index = lines.indexOf("ChatControl:");
                lines.set(index, "Chat:");
            }
            FileUtils.writeLines(messagesFile, lines);
            lines = FileUtils.readLines(messages_esFile, Charsets.UTF_8);
            if (lines.contains("ChatControl:")) {
                index = lines.indexOf("ChatControl:");
                lines.set(index, "Chat:");
            }
            FileUtils.writeLines(messages_esFile, lines);
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public File getMessagesFile() {
        return messagesFile;
    }
}
