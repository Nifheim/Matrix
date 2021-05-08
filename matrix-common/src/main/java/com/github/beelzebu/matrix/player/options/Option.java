package com.github.beelzebu.matrix.player.options;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.MatrixAPI;
import com.github.beelzebu.matrix.api.i18n.I18n;
import com.github.beelzebu.matrix.api.i18n.Message;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import org.jetbrains.annotations.NotNull;

public abstract class Option {

    protected final MatrixAPI api = Matrix.getAPI();
    protected final MatrixPlayer p;
    protected boolean enabled;

    public Option(MatrixPlayer p, boolean enabled) {
        this.p = p;
        this.enabled = enabled;
    }

    public final @NotNull String getStatus() {
        return enabled ? I18n.tl(Message.MENU_OPTIONS_STATUS_ENABLED, p.getLastLocale()) : I18n.tl(Message.MENU_OPTIONS_STATUS_DISABLED, p.getLastLocale());
    }

    public boolean isEnabled() {
        return enabled;
    }
}
