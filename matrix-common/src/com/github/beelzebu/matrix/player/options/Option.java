package com.github.beelzebu.matrix.player.options;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;

public abstract class Option {

    protected final MatrixAPI api = Matrix.getAPI();
    protected final MatrixPlayer p;
    protected boolean enabled;

    public Option(MatrixPlayer p, boolean enabled) {
        this.p = p;
        this.enabled = enabled;
    }

    public final String getStatus() {
        return enabled ? api.getString("Options.Status.Enabled", api.getPlugin().getLocale(p.getUniqueId())) : api.getString("Options.Status.Disabled", api.getPlugin().getLocale(p.getUniqueId()));
    }

    public boolean isEnabled() {
        return enabled;
    }
}
