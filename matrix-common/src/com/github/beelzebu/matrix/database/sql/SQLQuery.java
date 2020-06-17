package com.github.beelzebu.matrix.database.sql;

/**
 * @author Beelzebu
 */
public enum SQLQuery {
    /**
     * Params:
     * <ol>
     *     <li>uniqueId</li>
     *     <li>server</li>
     *     <li>kills</li>
     *     <li>mobKills</li>
     *     <li>deaths</li>
     *     <li>blocksBroken</li>
     *     <li>blocksPlaced</li>
     * </ol>
     */
    INSERT_STATS("{CALL insert_stats(?, ?, ?, ? ,?, ?, ?)}"),
    UPDATE_STATS_UUID("{CALL update_stats_uuid(?, ?)}"),
    /**
     * Params:
     * <ol>
     *     <li>uniqueId</li>
     *     <li>server</li>
     *     <li>message</li>
     * </ol>
     */
    INSERT_LOGIN("INSERT INTO matrix_failed_login(uniqueId, server, message) VALUES(?, ?, ?)"),
    SELECT_KILLS("SELECT kills FROM matrix_stats_total WHERE uniqueId = ? AND server = ?"),
    SELECT_MOB_KILLS("SELECT mobKills FROM matrix_stats_total WHERE uniqueId = ? AND server = ?"),
    SELECT_DEATHS("SELECT deaths FROM matrix_stats_total WHERE uniqueId = ? AND server = ?"),
    SELECT_BLOCKS_BROKEN("SELECT blocksBroken FROM matrix_stats_total WHERE uniqueId = ? AND server = ?"),
    SELECT_BLOCKS_PLACED("SELECT blocksPlaced FROM matrix_stats_total WHERE uniqueId = ? AND server = ?");

    private final String query;

    SQLQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
