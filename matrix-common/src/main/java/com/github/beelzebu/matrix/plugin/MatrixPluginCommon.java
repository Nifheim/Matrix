package com.github.beelzebu.matrix.plugin;

import com.github.beelzebu.matrix.api.plugin.MatrixPlugin;
import com.github.beelzebu.matrix.config.MatrixConfiguration;

public interface MatrixPluginCommon extends MatrixPlugin {

    MatrixConfiguration getMatrixConfiguration();
}
