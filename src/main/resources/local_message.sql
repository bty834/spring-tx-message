CREATE TABLE `your_table_name`
(
    `id`              bigint       NOT NULL AUTO_INCREMENT,
    `topic`           varchar(255) NOT NULL,
    `sharding_key`    varchar(255)          DEFAULT NULL,
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
    KEY `idx_updatetime` (`update_time`),
    KEY `idx_nextretrytime_retrytimes_sendstatus_deleted` (`send_status`,`next_retry_time`, `retry_times`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4
