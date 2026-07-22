package net.nifheim.matrix.auth.proxy.listener;

import com.velocitypowered.api.event.EventTask;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.nifheim.matrix.api.MatrixProvider;
import net.nifheim.matrix.api.player.MatrixPlayer;

public abstract class MatrixPlayerListener {

    protected EventTask wrapTask(CompletableFuture<Void> future) {
        return EventTask.resumeWhenComplete(future);
    }

    protected CompletableFuture<Void> executePlayerLogic(UUID uniqueId, Consumer<MatrixPlayer> logic) {
        return MatrixProvider.getAPI().getPlayerManager().getPlayer(uniqueId).thenAccept(logic);
    }
}
