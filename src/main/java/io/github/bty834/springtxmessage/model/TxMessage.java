package io.github.bty834.springtxmessage.model;

import io.github.bty834.springtxmessage.config.Note;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * <code>
 *     CREATE TABLE `your_table_name` <br/>
 * (<br/>
 *     `id`              bigint       NOT NULL,<br/>
 *     `topic`           varchar(255) NOT NULL,<br/>
 *     `key`             varchar(255)          DEFAULT NULL,<br/>
 *     `msg_id`          varchar(255)          DEFAULT NULL,<br/>
 *     `send_status`     tinyint      NOT NULL DEFAULT '0',<br/>
 *     `content`         longtext     NOT NULL,<br/>
 *     `retry_times`     tinyint      NOT NULL DEFAULT '0',<br/>
 *     `next_retry_time` datetime     NOT NULL,<br/>
 *     `deleted`         tinyint      NOT NULL DEFAULT '0',<br/>
 *     `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,<br/>
 *     `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,<br/>
 *     PRIMARY KEY (`id`),<br/>
 *     KEY `idx_createtime` (`create_time`),<br/>
 *     KEY `idx_msgid` (`msg_id`),<br/>
 *     KEY `idx_nextretrytime_sendstatus_deleted` (`next_retry_time`, `send_status`, `deleted`),<br/>
 *     KEY `idx_updatetime` (`update_time`)<br/>
 * ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4<br/>
 * </code>
 */
@Data
public class TxMessage {
    private Long id;
    private String topic;
    private String key;
    @Nullable
    @Note("send success callbacks insert this field")
    private String msgId;
    private SendStatus sendStatus;
    @Note("json string")
    private String content;
    private Integer retryTimes;
    @Note("send failed set nextRetryTime")
    private LocalDate nextRetryTime;
    private Boolean deleted;
    private LocalDate createTime;
    private LocalDate updateTime;
}
