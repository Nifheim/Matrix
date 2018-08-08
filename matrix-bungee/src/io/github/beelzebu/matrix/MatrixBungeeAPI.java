package io.github.beelzebu.matrix;

import io.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import io.github.beelzebu.matrix.player.BungeeMatrixPlayer;
import java.util.UUID;

/**
 * @author Beelzebu
 */
public class MatrixBungeeAPI extends MatrixCommonAPI {

    MatrixBungeeAPI(MatrixPlugin plugin) {
        super(plugin);
    }

    @Override
    public BungeeMatrixPlayer getPlayer(UUID uniqueId) {
        return getGson().fromJson(getGson().toJson(super.getPlayer(uniqueId)), BungeeMatrixPlayer.class);
    }

    @Override
    public BungeeMatrixPlayer getPlayer(String name) {
        return getGson().fromJson(getGson().toJson(super.getPlayer(name)), BungeeMatrixPlayer.class);
    }
}
