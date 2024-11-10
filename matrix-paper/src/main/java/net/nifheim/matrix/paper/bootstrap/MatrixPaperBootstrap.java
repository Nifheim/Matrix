package net.nifheim.matrix.paper.bootstrap;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import net.nifheim.matrix.common.server.ServerPlatformInfo;
import net.nifheim.matrix.paper.MatrixPaper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class MatrixPaperBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(@NotNull BootstrapContext bootstrapContext) {
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
        FileConfiguration environment = YamlConfiguration.loadConfiguration(context.getDataDirectory().resolve("environment.yml").toFile());
        return new MatrixPaper(new ServerPlatformInfo(environment.getString("server.host"), environment.getInt("server.port"), Bukkit.getMaxPlayers()));
    }
}
