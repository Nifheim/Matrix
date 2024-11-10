package net.nifheim.matrix.common.database.sql;

/**
 * @author Jaime Suárez
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
    INSERT_FAILED_LOGIN("INSERT INTO failed_login(uniqueId, server, message) VALUES(?, ?, ?)"),
    INSERT_COMMAND_LOG("INSERT INTO command_log(user_id, server, command) VALUES (?, ?, ?)"),
    SELECT_PLAY_STATS("SELECT joins, total_play_time FROM play_stats_total WHERE user_id = ? AND game_type = ?"),
    SELECT_KILLS_TOTAL("SELECT kills FROM stats_total WHERE id = ? AND server = ?"),
    SELECT_MOB_KILLS_TOTAL("SELECT mobKills FROM stats_total WHERE id = ? AND server = ?"),
    SELECT_DEATHS_TOTAL("SELECT deaths FROM stats_total WHERE id = ? AND server = ?"),
    SELECT_BLOCKS_BROKEN_TOTAL("SELECT blocksBroken FROM stats_total WHERE id = ? AND server = ?"),
    SELECT_BLOCKS_PLACED_TOTAL("SELECT blocksPlaced FROM stats_total WHERE id = ? AND server = ?"),
    SELECT_KILLS_WEEKLY("SELECT kills FROM stats_weekly WHERE id = ? AND server = ?"),
    SELECT_MOB_KILLS_WEEKLY("SELECT mobKills FROM stats_weekly WHERE id = ? AND server = ?"),
    SELECT_DEATHS_WEEKLY("SELECT deaths FROM stats_weekly WHERE id = ? AND server = ?"),
    SELECT_BLOCKS_BROKEN_WEEKLY("SELECT blocksBroken FROM stats_weekly WHERE id = ? AND server = ?"),
    SELECT_BLOCKS_PLACED_WEEKLY("SELECT blocksPlaced FROM stats_weekly WHERE id = ? AND server = ?"),
    SELECT_KILLS_MONTHLY("SELECT kills FROM stats_monthly WHERE id = ? AND server = ?"),
    SELECT_MOB_KILLS_MONTHLY("SELECT mobKills FROM stats_monthly WHERE id = ? AND server = ?"),
    SELECT_DEATHS_MONTHLY("SELECT deaths FROM stats_monthly WHERE id = ? AND server = ?"),
    SELECT_BLOCKS_BROKEN_MONTHLY("SELECT blocksBroken FROM stats_monthly WHERE id = ? AND server = ?"),
    SELECT_BLOCKS_PLACED_MONTHLY("SELECT blocksPlaced FROM stats_monthly WHERE id = ? AND server = ?"),
    SELECT_KILLS_TOP_TOTAL("SELECT kills, id FROM stats_total WHERE server = ? ORDER BY kills DESC LIMIT 10"),
    SELECT_MOB_KILLS_TOP_TOTAL("SELECT mobKills, id FROM stats_total WHERE server = ? ORDER BY mobKills DESC LIMIT 10"),
    SELECT_DEATHS_TOP_TOTAL("SELECT deaths, id FROM stats_total WHERE server = ? ORDER BY deaths DESC LIMIT 10"),
    SELECT_BLOCKS_BROKEN_TOP_TOTAL("SELECT blocksBroken, id FROM stats_total WHERE server = ? ORDER BY blocksBroken DESC LIMIT 10"),
    SELECT_BLOCKS_PLACED_TOP_TOTAL("SELECT blocksPlaced, id FROM stats_total WHERE server = ? ORDER BY blocksPlaced DESC LIMIT 10"),
    SELECT_KILLS_TOP_WEEKLY("SELECT kills, id FROM stats_weekly WHERE server = ? ORDER BY kills DESC LIMIT 10"),
    SELECT_MOB_KILLS_TOP_WEEKLY("SELECT mobKills, id FROM stats_weekly WHERE server = ? ORDER BY mobKills DESC LIMIT 10"),
    SELECT_DEATHS_TOP_WEEKLY("SELECT deaths, id FROM stats_weekly WHERE server = ? ORDER BY deaths DESC LIMIT 10"),
    SELECT_BLOCKS_BROKEN_TOP_WEEKLY("SELECT blocksBroken, id FROM stats_weekly WHERE server = ? ORDER BY blocksBroken DESC LIMIT 10"),
    SELECT_BLOCKS_PLACED_TOP_WEEKLY("SELECT blocksPlaced, id FROM stats_weekly WHERE server = ? ORDER BY blocksPlaced DESC LIMIT 10"),
    SELECT_KILLS_TOP_MONTHLY("SELECT kills, id FROM stats_monthly WHERE server = ? ORDER BY kills DESC LIMIT 10"),
    SELECT_MOB_KILLS_TOP_MONTHLY("SELECT mobKills, id FROM stats_monthly WHERE server = ? ORDER BY mobKills DESC LIMIT 10"),
    SELECT_DEATHS_TOP_MONTHLY("SELECT deaths, id FROM stats_monthly WHERE server = ? ORDER BY deaths DESC LIMIT 10"),
    SELECT_BLOCKS_BROKEN_TOP_MONTHLY("SELECT blocksBroken, id FROM stats_monthly WHERE server = ? ORDER BY blocksBroken DESC LIMIT 10"),
    SELECT_BLOCKS_PLACED_TOP_MONTHLY("SELECT blocksPlaced, id FROM stats_monthly WHERE server = ? ORDER BY blocksPlaced DESC LIMIT 10"),
    INSERT_HANDSHAKE("INSERT INTO handshake(ip, protocol, version, hostname) VALUES (?, ?, ?, ?)"),
    INSERT_PLAYER("INSERT INTO player(hexId, uniqueId, name) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE hexId = ?, uniqueId = ?, name = ?"),
    SELECT_PLAYER_BY_ID("SELECT hexId, uniqueId, name FROM player WHERE hexId = ?"),
    SELECT_PLAYER_BY_UUID("SELECT hexId, uniqueId, name FROM player WHERE uniqueId = ?"),
    INSERT_PLAYER_LOGIN("INSERT INTO player_login(player_id) SELECT id FROM player WHERE hexId = ?"),
    INSERT_PLAYER_LOGOUT("INSERT INTO player_logout(player_id) SELECT id FROM player WHERE hexId = ?"),
    INSERT_PLAYER_ADDRESS("INSERT INTO player_address(id, ip) SELECT id, ? FROM player WHERE hexId = ?"),
    INSERT_PLAY_STATS("INSERT INTO play_stats(player_id, server_group, play_time) VALUES ((SELECT id FROM player WHERE hexId = ?), ?, ?)");


    private final String query;

    SQLQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
