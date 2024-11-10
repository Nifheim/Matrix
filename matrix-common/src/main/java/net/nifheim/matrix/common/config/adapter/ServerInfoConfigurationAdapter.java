package net.nifheim.matrix.common.config.adapter;

import net.nifheim.matrix.api.player.gamemode.GameMode;
import net.nifheim.matrix.api.server.ServerType;
import net.nifheim.matrix.common.config.FileConfigurationWrapper;
import net.nifheim.matrix.common.config.sub.ServerInfoConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Adapter class to convert a {@link FileConfigurationWrapper} to a {@link ServerInfoConfiguration}.
 *
 * @author Jaime Suárez
 */
public class ServerInfoConfigurationAdapter implements ServerInfoConfiguration {

    private static final String DEFAULT_GROUP_NAME = "default";

    private final FileConfigurationWrapper config;

    public ServerInfoConfigurationAdapter(@NotNull FileConfigurationWrapper config) {
        this.config = config;
    }

    @Override
    public @NotNull GameMode getGameMode() {
        String gameMode = config.getString("server-info.game-mode");
        return gameMode != null ? GameMode.valueOf(gameMode.toUpperCase()) : GameMode.SURVIVAL;
    }

    @Override
    public @NotNull String getGroupName() {
        return config.getString("server-info.group-name", DEFAULT_GROUP_NAME);
    }

    @Override
    public @Nullable String getServerName() {
        return config.getString("server-info.server-name", null);
    }

    @Override
    public @Nullable String getLobbyServer() {
        return config.getString("server-info.lobby-server");
    }

    @Override
    public @NotNull ServerType getServerType() {
        String serverType = config.getString("server-info.server-type");
        return serverType != null ? ServerType.valueOf(serverType.toUpperCase()) : ServerType.SURVIVAL;
    }
}
