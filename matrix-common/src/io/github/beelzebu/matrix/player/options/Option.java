package io.github.beelzebu.matrix.player.options;

import io.github.beelzebu.matrix.api.Matrix;
import io.github.beelzebu.matrix.api.MatrixAPI;
import io.github.beelzebu.matrix.api.player.MatrixPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public abstract class Option {

    protected final MatrixAPI api = Matrix.getAPI();
    protected final MatrixPlayer p;
    @Getter
    protected boolean enabled;

    public final String getStatus() {
        return enabled ? api.getString("Options.Status.Enabled", api.getPlugin().getLocale(p.getUniqueId())) : api.getString("Options.Status.Disabled", api.getPlugin().getLocale(p.getUniqueId()));
    }
}
