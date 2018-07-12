package io.github.beelzebu.matrix.player.options;

import io.github.beelzebu.matrix.MatrixAPI;
import io.github.beelzebu.matrix.player.MatrixPlayer;
import lombok.Getter;

public abstract class Option {

    protected final MatrixAPI core = MatrixAPI.getInstance();
    protected final MatrixPlayer p;
    @Getter
    protected boolean enabled = false;

    public Option(MatrixPlayer player, boolean status) {
        p = player;
        enabled = status;
    }

    public final String getStatus() {
        return enabled ? core.getString("Options.Status.Enabled", core.getMethods().getLocale(p.getUniqueId())) : core.getString("Options.Status.Disabled", core.getMethods().getLocale(p.getUniqueId()));
    }
}
