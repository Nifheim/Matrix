package net.nifheim.matrix.api.server;

import net.nifheim.matrix.api.player.gamemode.GameMode;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * Factory for creating server info objects. To create a new server info is needed a group name, a raw server name (optional), a server type, a game mode, an address, a port, a max players and a
 * lobby.
 * </p>
 * <p>
 * With that information the factory will generate a server name using {@link ServerName} to pass it to the server info object.
 * </p>
 *
 * @author Jaime Suárez
 * @see ServerInfo
 */
public interface ServerInfoFactory {

    /**
     * Creates a new server info object.
     *
     * @param groupName  the group name
     * @param serverType the server type
     * @param gameMode   the game mode
     * @param address    the address
     * @param port       the port
     * @param maxPlayers the max players
     * @return the server info object
     */
    ServerInfo createServerInfo(String groupName, ServerType serverType, GameMode gameMode, String address, int port, int maxPlayers);

    /**
     * Creates a new server info object.
     *
     * @param groupName     the group name
     * @param rawServerName the raw server name
     * @param serverType    the server type
     * @param gameMode      the game mode
     * @param address       the address
     * @param port          the port
     * @param maxPlayers    the max players
     * @return the server info object
     */
    ServerInfo createServerInfo(String groupName, @Nullable String rawServerName, ServerType serverType, GameMode gameMode, String address, int port, int maxPlayers);
}
