package com.github.beelzebu.matrix.player.options;

import cl.indiopikaro.jmatrix.api.Matrix;
import cl.indiopikaro.jmatrix.api.MatrixAPI;
import cl.indiopikaro.jmatrix.api.i18n.I18n;
import cl.indiopikaro.jmatrix.api.i18n.Message;
import cl.indiopikaro.jmatrix.api.player.MatrixPlayer;

public abstract class Option {

    protected final MatrixAPI api = Matrix.getAPI();
    protected final MatrixPlayer p;
    protected boolean enabled;

    public Option(MatrixPlayer p, boolean enabled) {
        this.p = p;
        this.enabled = enabled;
    }

    public final String getStatus() {
        return enabled ? I18n.tl(Message.MENU_OPTIONS_STATUS_ENABLED, p.getLastLocale()) : I18n.tl(Message.MENU_OPTIONS_STATUS_DISABLED, p.getLastLocale());
    }

    public boolean isEnabled() {
        return enabled;
    }
}
