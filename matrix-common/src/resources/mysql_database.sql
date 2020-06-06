-- drop tables, events and procedures
DROP TABLE IF EXISTS matrix_stats_total;
DROP TABLE IF EXISTS matrix_stats_monthly;
DROP TABLE IF EXISTS matrix_stats_weekly;
DROP TABLE IF EXISTS matrix_failed_login;
DROP PROCEDURE IF EXISTS insert_stats;
DROP EVENT IF EXISTS drop_monthly_stats;
DROP EVENT IF EXISTS drop_weekly_stats;

-- create tables
CREATE TABLE IF NOT EXISTS matrix_stats_total
(
    uniqueId     CHAR(36) PRIMARY KEY NOT NULL,
    server       VARCHAR(35)          NOT NULL,
    kills        LONG                 NOT NULL DEFAULT 0,
    mobKills     LONG                 NOT NULL DEFAULT 0,
    deaths       LONG                 NOT NULL DEFAULT 0,
    blocksBroken LONG                 NOT NULL DEFAULT 0,
    blocksPlaced LONG                 NOT NULL DEFAULT 0,
    UNIQUE KEY user_server_uq (uniqueId, server)
);

CREATE TABLE IF NOT EXISTS matrix_stats_monthly LIKE matrix_stats_total;

CREATE TABLE IF NOT EXISTS matrix_stats_weekly LIKE matrix_stats_total;

CREATE TABLE IF NOT EXISTS matrix_failed_login
(
    date     DATETIME    NOT NULL DEFAULT SYSDATE(),
    uniqueId CHAR(36)    NOT NULL,
    server   VARCHAR(35) NOT NULL,
    message  VARCHAR(1000)        DEFAULT NULL,
    UNIQUE KEY date_user (date, uniqueId)
);

CREATE OR REPLACE PROCEDURE insert_stats(v_uniqueId CHAR(36), v_server VARCHAR(35), v_kills LONG, v_mobKills LONG,
                                         v_deaths LONG, v_blocksBroken LONG, v_blocksPlaced LONG)
BEGIN
    IF (SELECT COUNT(uniqueId) FROM matrix_stats_weekly WHERE uniqueId = v_uniqueId AND server = v_server) = 1 THEN
        UPDATE matrix_stats_weekly
        SET kills        = kills + v_kills,
            mobKills     = mobKills + v_mobKills,
            deaths       = deaths + v_deaths,
            blocksPlaced = blocksPlaced + v_blocksPlaced,
            blocksBroken = blocksBroken + v_blocksBroken
        WHERE uniqueId = v_uniqueId
          AND server = v_server;
    ELSE
        INSERT INTO matrix_stats_weekly(uniqueId, server, kills, mobKills, blocksBroken, blocksPlaced)
        VALUES (v_uniqueId, v_server, v_kills, v_mobKills, v_blocksBroken, v_blocksPlaced);
    END IF;
    IF (SELECT COUNT(uniqueId) FROM matrix_stats_monthly WHERE uniqueId = v_uniqueId AND server = v_server) = 1 THEN
        UPDATE matrix_stats_monthly
        SET kills        = kills + v_kills,
            mobKills     = mobKills + v_mobKills,
            deaths       = deaths + v_deaths,
            blocksPlaced = blocksPlaced + v_blocksPlaced,
            blocksBroken = blocksBroken + v_blocksBroken
        WHERE uniqueId = v_uniqueId
          AND server = v_server;
    ELSE
        INSERT INTO matrix_stats_monthly(uniqueId, server, kills, mobKills, blocksBroken, blocksPlaced)
        VALUES (v_uniqueId, v_server, v_kills, v_mobKills, v_blocksBroken, v_blocksPlaced);
    END IF;
    IF (SELECT COUNT(uniqueId) FROM matrix_stats_total WHERE uniqueId = v_uniqueId AND server = v_server) = 1 THEN
        UPDATE matrix_stats_total
        SET kills        = kills + v_kills,
            mobKills     = mobKills + v_mobKills,
            deaths       = deaths + v_deaths,
            blocksPlaced = blocksPlaced + v_blocksPlaced,
            blocksBroken = blocksBroken + v_blocksBroken
        WHERE uniqueId = v_uniqueId
          AND server = v_server;
    ELSE
        INSERT INTO matrix_stats_total(uniqueId, server, kills, mobKills, blocksBroken, blocksPlaced)
        VALUES (v_uniqueId, v_server, v_kills, v_mobKills, v_blocksBroken, v_blocksPlaced);
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