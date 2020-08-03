package com.github.beelzebu.matrix.plugin;

import com.github.beelzebu.matrix.Main;
import cl.indiopikaro.jmatrix.api.config.AbstractConfig;
import cl.indiopikaro.jmatrix.api.plugin.MatrixPlugin;
import com.github.beelzebu.matrix.config.ExternalConfig;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * @author Beelzebu
 */
public class MatrixExternalPlugin implements MatrixPlugin {

    private final Main main;
    private final ExternalConfig config = new ExternalConfig(main, main.getConfigFile());
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    public MatrixExternalPlugin(Main main) {
        this.main = main;
    }

    @Override
    public void log(String message) {
    }

    @Override
    public AbstractConfig getConfig() {
        return config;
    }

    @Override
    public AbstractConfig getFileAsConfig(File file) {
        return null;
    }

    @Override
    public void runAsync(Runnable runnable) {
        executor.execute(runnable);
    }

    @Override
    public void runAsync(Runnable runnable, Integer timer) {

    }

    @Override
    public void runSync(Runnable runnable) {
        executor.execute(runnable);
    }

    @Override
    public void executeCommand(String command) {
        executor.execute(() -> {
            try {
                Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public Object getConsole() {
        return null;
    }

    @Override
    public void sendMessage(Object CommandSender, BaseComponent[] message) {
    }

    @Override
    public void sendMessage(String name, String message) {
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
    }

    @Override
    public File getDataFolder() {
        return null;
    }

    @Override
    public InputStream getResource(String filename) {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public boolean isOnline(String name, boolean here) {
        return false;
    }

    @Override
    public boolean isOnline(UUID uuid, boolean here) {
        return false;
    }

    @Override
    public void callLevelUPEvent(UUID uuid, long newexp, long oldexp) {

    }

    @Override
    public String getLocale(UUID uuid) {
        return null;
    }

    @Override
    public void ban(String name) {

    }
}
