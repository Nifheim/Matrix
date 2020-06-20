-- drop tables, events and procedures
DROP TABLE IF EXISTS matrix_stats_total;
DROP TABLE IF EXISTS matrix_stats_monthly;
DROP TABLE IF EXISTS matrix_stats_weekly;
DROP TABLE IF EXISTS matrix_failed_login;
DROP TABLE IF EXISTS matrix_command_log;
DROP TABLE IF EXISTS matrix_play_stats;
DROP VIEW IF EXISTS matrix_play_stats_total;
DROP PROCEDURE IF EXISTS insert_stats;
DROP EVENT IF EXISTS drop_monthly_stats;
DROP EVENT IF EXISTS drop_weekly_stats;

-- create tables
CREATE TABLE IF NOT EXISTS matrix_stats_total
(
    id           CHAR(24) PRIMARY KEY NOT NULL,
    server       VARCHAR(35)          NOT NULL,
    kills        LONG                 NOT NULL DEFAULT 0,
    mobKills     LONG                 NOT NULL DEFAULT 0,
    deaths       LONG                 NOT NULL DEFAULT 0,
    blocksBroken LONG                 NOT NULL DEFAULT 0,
    blocksPlaced LONG                 NOT NULL DEFAULT 0,
    UNIQUE KEY user_server_uq (id, server)
);

CREATE TABLE IF NOT EXISTS matrix_stats_monthly LIKE matrix_stats_total;

CREATE TABLE IF NOT EXISTS matrix_stats_weekly LIKE matrix_stats_total;

CREATE TABLE IF NOT EXISTS matrix_failed_login
(
    date     DATETIME    NOT NULL DEFAULT SYSDATE(),
    uniqueId CHAR(36)    NOT NULL,
    name     VARCHAR(16) NOT NULL,
    server   VARCHAR(35) NOT NULL,
    message  VARCHAR(1000)        DEFAULT NULL,
    UNIQUE KEY date_user (date, uniqueId)
);

CREATE TABLE IF NOT EXISTS matrix_command_log
(
    id      INT          NOT NULL AUTO_INCREMENT,
    user_id CHAR(24)     NOT NULL,
    server  VARCHAR(35)  NOT NULL,
    command VARCHAR(256) NOT NULL,
    date    DATETIME     NOT NULL DEFAULT sysdate(),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS matrix_play_stats
(
    id        INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    user_id   CHAR(24)        NOT NULL PRIMARY KEY,
    game_type ENUM ('TOWNY', 'SURVIVAL', 'SKYWARS', 'SKYBLOCK', 'BEDWARS', 'EGGWARS', 'KITPVP', 'RFTB', 'SSG', 'FULLPVP', 'TNTRUN', 'BUILDBATTLE', 'UHC', 'INFLUENCER'),
    play_time LONG,
    date      DATETIME        NOT NULL DEFAULT sysdate()
);

CREATE VIEW IF NOT EXISTS matrix_play_stats_total AS
SELECT count(id) joins, sum(play_time) total_play_time, game_type, user_id
FROM matrix_play_stats
GROUP BY game_type, user_id;

CREATE OR REPLACE PROCEDURE insert_stats(v_id CHAR(24), v_server VARCHAR(35), v_kills LONG, v_mobKills LONG,
                                         v_deaths LONG, v_blocksBroken LONG, v_blocksPlaced LONG)
BEGIN
    IF (SELECT COUNT(id) FROM matrix_stats_weekly WHERE id = v_id AND server = v_server) = 1 THEN
        UPDATE matrix_stats_weekly
        SET kills        = kills + v_kills,
            mobKills     = mobKills + v_mobKills,
            deaths       = deaths + v_deaths,
            blocksPlaced = blocksPlaced + v_blocksPlaced,
            blocksBroken = blocksBroken + v_blocksBroken
        WHERE id = v_id
          AND server = v_server;
    ELSE
        INSERT INTO matrix_stats_weekly(id, server, kills, mobKills, blocksBroken, blocksPlaced)
        VALUES (v_id, v_server, v_kills, v_mobKills, v_blocksBroken, v_blocksPlaced);
    END IF;
    IF (SELECT COUNT(id) FROM matrix_stats_monthly WHERE id = v_id AND server = v_server) = 1 THEN
        UPDATE matrix_stats_monthly
        SET kills        = kills + v_kills,
            mobKills     = mobKills + v_mobKills,
            deaths       = deaths + v_deaths,
            blocksPlaced = blocksPlaced + v_blocksPlaced,
            blocksBroken = blocksBroken + v_blocksBroken
        WHERE id = v_id
          AND server = v_server;
    ELSE
        INSERT INTO matrix_stats_monthly(id, server, kills, mobKills, blocksBroken, blocksPlaced)
        VALUES (v_id, v_server, v_kills, v_mobKills, v_blocksBroken, v_blocksPlaced);
    END IF;
    IF (SELECT COUNT(id) FROM matrix_stats_total WHERE id = v_id AND server = v_server) = 1 THEN
        UPDATE matrix_stats_total
        SET kills        = kills + v_kills,
            mobKills     = mobKills + v_mobKills,
            deaths       = deaths + v_deaths,
            blocksPlaced = blocksPlaced + v_blocksPlaced,
            blocksBroken = blocksBroken + v_blocksBroken
        WHERE id = v_id
          AND server = v_server;
    ELSE
        INSERT INTO matrix_stats_total(id, server, kills, mobKills, blocksBroken, blocksPlaced)
        VALUES (v_id, v_server, v_kills, v_mobKills, v_blocksBroken, v_blocksPlaced);
    END IF;
END;

CREATE EVENT drop_monthly_stats ON SCHEDULE EVERY 1 MONTH
    STARTS date(concat(year(curdate()), '-', month(curdate()) + 1, '-1')) DO
    BEGIN
        DELETE
        FROM matrix_stats_monthly
        WHERE TRUE;
    END;

CREATE EVENT drop_weekly_stats ON SCHEDULE EVERY 1 WEEK
    STARTS date(
            concat(year(curdate()), '-', month(curdate()) + 1, '-',
                   day(curdate()) + 1 + (7 - (weekday(curdate()) + 1)))) DO
    BEGIN
        DELETE
        FROM matrix_stats_weekly
        WHERE TRUE;
    END;