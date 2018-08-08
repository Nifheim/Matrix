package io.github.beelzebu.matrix;

import io.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import io.github.beelzebu.matrix.player.BukkitMatrixPlayer;
import java.util.UUID;

/**
 * @author Beelzebu
 */
public class MatrixBukkitAPI extends MatrixCommonAPI {

    MatrixBukkitAPI(MatrixPlugin plugin) {
        super(plugin);
    }

    @Override
    public BukkitMatrixPlayer getPlayer(UUID uniqueId) {
        return getGson().fromJson(getGson().toJson(super.getPlayer(uniqueId)), BukkitMatrixPlayer.class);
    }

    @Override
    public BukkitMatrixPlayer getPlayer(String name) {
        return getGson().fromJson(getGson().toJson(super.getPlayer(name)), BukkitMatrixPlayer.class);
    }
}
