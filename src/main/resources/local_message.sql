CREATE TABLE `local_table`
(
    `id`              bigint       NOT NULL,
    `topic`           varchar(255) NOT NULL,
    `key`             varchar(255)          DEFAULT NULL,
    `msg_id`          varchar(255)          DEFAULT NULL,
    `send_status`     tinyint      NOT NULL DEFAULT '0',
    `content`         longtext     NOT NULL,
    `retry_times`     tinyint      NOT NULL DEFAULT '0',
    `next_retry_time` datetime     NOT NULL,
    `deleted`         tinyint      NOT NULL DEFAULT '0',
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_createtime` (`create_time`),
    KEY `idx_msgid` (`msg_id`),
    KEY `idx_nextretrytime_sendstatus_deleted` (`next_retry_time`, `send_status`, `deleted`),
    KEY `idx_updatetime` (`update_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4