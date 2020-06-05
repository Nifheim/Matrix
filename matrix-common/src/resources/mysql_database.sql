CREATE TABLE IF NOT EXISTS matrix_stats
(
    uniqueId     CHAR(36) PRIMARY KEY NOT NULL,
    server       VARCHAR(35)          NOT NULL,
    kills        LONG                 NOT NULL DEFAULT 0,
    mobKills     LONG                 NOT NULL DEFAULT 0,
    blocksBroken LONG                 NOT NULL DEFAULT 0,
    blocksPlaced LONG                 NOT NULL DEFAULT 0,
    unique key user_server_uq (uniqueId, server)
);

CREATE TABLE IF NOT EXISTS matrix_stats_monthly
(
    uniqueId     CHAR(36) PRIMARY KEY NOT NULL,
    server       VARCHAR(35)          NOT NULL,
    kills        LONG                 NOT NULL DEFAULT 0,
    mobKills     LONG                 NOT NULL DEFAULT 0,
    blocksBroken LONG                 NOT NULL DEFAULT 0,
    blocksPlaced LONG                 NOT NULL DEFAULT 0,
    unique key user_server_uq (uniqueId, server)
);

CREATE TABLE IF NOT EXISTS matrix_failed_login
(
    date     DATETIME    NOT NULL DEFAULT SYSDATE(),
    uniqueId CHAR(36)    NOT NULL,
    server   VARCHAR(35) NOT NULL,
    message  VARCHAR(1000)        DEFAULT NULL,
    unique key date_user (date, uniqueId)
);

CREATE OR REPLACE PROCEDURE move_stats()
begin
    delete from matrix_stats_monthly where true;
    insert into matrix_stats_monthly select * from matrix_stats;
end;