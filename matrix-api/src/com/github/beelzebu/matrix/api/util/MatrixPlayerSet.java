package com.github.beelzebu.matrix.api.util;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import java.util.HashSet;

/**
 * @author Beelzebu
 */
public class MatrixPlayerSet <T extends MatrixPlayer> extends HashSet<T> {

    @Override
    public boolean add(T t) {
        if (t == null) {
            return false;
        }
        if (t.getUniqueId() == null) {
            return false;
        }
        if (t.getName() == null) {
            return false;
        }
        for (MatrixPlayer matrixPlayer : this) {
            if (matrixPlayer.getUniqueId() == t.getUniqueId()) {
                // debug player removing
                Matrix.getLogger().debug(new Exception("Overriding player on players set"));
            }
        }
        return super.add(t);
    }
}
