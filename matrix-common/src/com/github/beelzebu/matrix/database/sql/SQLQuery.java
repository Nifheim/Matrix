package com.github.beelzebu.matrix.database.sql;

/**
 * @author Beelzebu
 */
public enum SQLQuery {
    /**
     * Params:
     * <ol>
     *     <li>id</li>
     *     <li>server</li>
     *     <li>kills</li>
     *     <li>mobKills</li>
     *     <li>deaths</li>
     *     <li>blocksBroken</li>
     *     <li>blocksPlaced</li>
     * </ol>
     */
    INSERT_STATS("{CALL insert_stats(?, ?, ?, ? ,?, ?, ?)}"),
    /**
     * Params:
     * <ol>
     *     <li>uniqueId</li>
     *     <li>server</li>
     *     <li>message</li>
     * </ol>
     */
    INSERT_LOGIN("INSERT INTO matrix_failed_login(uniqueId, server, message) VALUES(?, ?, ?)"),
    INSERT_COMMAND_LOG("INSERT INTO matrix_command_log(user_id, server, command) VALUES (?, ?, ?)"),
    INSERT_PLAY_STATS("INSERT INTO matrix_play_stats(user_id, game_type, play_time) VALUES (?, ?, ?)"),
    SELECT_PLAY_STATS("SELECT joins, total_play_time FROM matrix_play_stats_total WHERE user_id = ? AND game_type = ?"),
    SELECT_KILLS("SELECT kills FROM matrix_stats_total WHERE id = ? AND server = ?"),
    SELECT_MOB_KILLS("SELECT mobKills FROM matrix_stats_total WHERE id = ? AND server = ?"),
    SELECT_DEATHS("SELECT deaths FROM matrix_stats_total WHERE id = ? AND server = ?"),
    SELECT_BLOCKS_BROKEN("SELECT blocksBroken FROM matrix_stats_total WHERE id = ? AND server = ?"),
    SELECT_BLOCKS_PLACED("SELECT blocksPlaced FROM matrix_stats_total WHERE id = ? AND server = ?");

    private final String query;

    SQLQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
