package com.github.beelzebu.matrix.tasks;

import com.github.games647.craftapi.resolver.MojangResolver;

/**
 * @author Beelzebu
 */
public interface IndioLoginTask extends Runnable {

    MojangResolver RESOLVER = new MojangResolver();
}
