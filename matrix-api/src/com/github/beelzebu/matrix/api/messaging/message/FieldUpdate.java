package com.github.beelzebu.matrix.api.messaging.message;

import com.github.beelzebu.matrix.api.Matrix;
import com.github.beelzebu.matrix.api.player.MatrixPlayer;
import java.util.UUID;

/**
 * @author Beelzebu
 */
public class FieldUpdate extends RedisMessage {

    private final UUID player;
    private final String field;
    private final String jsonValue;

    public FieldUpdate(UUID player, String field, String jsonValue) {
        super(RedisMessageType.FIELD_UPDATE);
        this.player = player;
        this.field = field;
        this.jsonValue = jsonValue;
    }

    public UUID getPlayer() {
        return player;
    }

    public String getField() {
        return field;
    }

    public String getJsonValue() {
        return jsonValue;
    }

    @Override
    public void read() {
        if (!api.getPlugin().isOnline(getPlayer(), true)) {
            return;
        }
        MatrixPlayer matrixPlayer = api.getPlayer(getPlayer());
        if (matrixPlayer == null) {
            Matrix.getLogger().info("Player with uuid '" + getPlayer() + "' is online but there is no MatrixPlayer associated to his uuid.");
            return;
        }
        matrixPlayer.setField(getField(), getJsonValue());
    }

    @Override
    protected boolean onlyExternal() {
        return true;
    }

}
