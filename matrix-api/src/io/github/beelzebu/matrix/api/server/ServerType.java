package io.github.beelzebu.matrix.api.server;

public enum ServerType {
    LOBBY, SURVIVAL, MINIGAME_BUNGEE, MINIGAME_MULTIARENA;

    public boolean isMinigame() {
        return this != LOBBY;
    }
}
