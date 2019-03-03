package io.github.beelzebu.matrix.api.server;

public enum ServerType {
    PROXY, LOBBY, SURVIVAL, MINIGAME_BUNGEE, MINIGAME_MULTIARENA, OTHER;

    public boolean isMinigame() {
        return this != LOBBY;
    }
}
