package io.github.bty834.springtxmessage.model;

import io.github.bty834.springtxmessage.config.Note;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.ToString;
import org.springframework.lang.Nullable;

/**
 * <code>
 CREATE TABLE `your_table_name` <br/>
 (<br/>
 `id`              bigint       NOT NULL AUTO_INCREMENT,<br/>
 `number`          bigint       NOT NULL ,<br/>
 `topic`           varchar(255) NOT NULL,<br/>
 `sharding_key`    varchar(255)          DEFAULT NULL,<br/>
 `msg_id`          varchar(255)          DEFAULT NULL,<br/>
 `send_status`     tinyint      NOT NULL DEFAULT '0',<br/>
 `content`         longtext     NOT NULL,<br/>
 `retry_times`     tinyint      NOT NULL DEFAULT '0',<br/>
 `next_retry_time` datetime     NOT NULL,<br/>
 `deleted`         tinyint      NOT NULL DEFAULT '0',<br/>
 `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,<br/>
 `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,<br/>
 PRIMARY KEY (`id`),<br/>
 KEY `idx_createtime` (`create_time`),<br/>
 KEY `idx_msgid` (`msg_id`),<br/>
 KEY `idx_updatetime` (`update_time`),<br/>
 KEY `idx_nextretrytime_retrytimes_sendstatus_deleted` (`send_status`,`next_retry_time`, `retry_times`, `deleted`)<br/>
 ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4<br/>
 * </code>
 */
@ToString
@Data
public class TxMessagePO {
    private Long id;
    private Long number;
    private String topic;
    private String shardingKey;
    @Nullable
    @Note("send success callbacks insert this field")
    private String msgId;
    private SendStatus sendStatus;
    @Note("json string")
    private String content;
    private Integer retryTimes;
    @Note("send failed set nextRetryTime")
    private LocalDateTime nextRetryTime;
    private Boolean deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static TxMessagePO convertFrom(TxMessage txMessage) {
        TxMessagePO po = new TxMessagePO();
        po.setContent(txMessage.getContent());
        po.setMsgId(txMessage.getMsgId());
        po.setTopic(txMessage.getTopic());
        po.setShardingKey(txMessage.getShardingKey());
        return po;
    }

    public TxMessage convertToTxMessage() {
        TxMessage txMessage = new TxMessage();
        txMessage.setContent(content);
        txMessage.setMsgId(msgId);
        txMessage.setTopic(topic);
        txMessage.setShardingKey(shardingKey);
        return txMessage;
    }
}
