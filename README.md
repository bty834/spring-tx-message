
# Quick Start

1. add dependency

```xml
   <dependency>
            <groupId>io.github.bty834</groupId>
            <artifactId>spring-tx-message</artifactId>
            <version>0.0.1-SNAPSHOT</version>
   </dependency>
```

2. create table and configure adapter and repository

```sql
CREATE TABLE `your_table_name`
(
    `id`              bigint       NOT NULL AUTO_INCREMENT,
    `number`          bigint       NOT NULL ,
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
```

```java
@Configuration
public class TxMessageConfig {
    @Bean
    public TxMessageSendAdapter txMessageSendAdapter() {
        return new MyMessageSendAdapter();
    }
    @Bean
    public TxMessageRepository txMessageRepository(DataSource dataSource) {
        // your_table_name is your table name 
        return new TxMessageRepository(dataSource, "your_table_name");
    }
}

/**
 * your send adapter
 */
class MyMessageSendAdapter implements TxMessageSendAdapter {
    
    // ...
    
    @Override
    public TxMessageSendResult send(TxMessage txMessage) {
        // your adapter logic
        TxMessageSendResult sendResult = new TxMessageSendResult();
        sendResult.setMsgId("xxx");
        sendResult.setSuccess(true);
        return sendResult;
    }
}
```

3. enable and use it: save or try send

set `spring.tx.message.send.enabled = true` to enable save and try send

```java
    @Autowired
    TxMessageSender txMessageSender;

    public void sendMsg(TxMessage txMessage) {
        // save but don't retry send
        txMessageSender.batchSave(Collections.singletonList(txMessage));
        txMessageSender.saveAndTrySend(txMessage);
        txMessageSender.batchSaveAndTrySend(Collections.singletonList(txMessage));
    }
```

4. enabled use it: compensate send

set `spring.tx.message.compensate.send.enabled = true` to enable compensate send

   set `spring.tx.message.compensate.interval.seconds`  to customize compensate intervals
```java
    @Autowired
    TxMessageCompensateSender compensateSender;
    
    public void compensateSend() {
        // send with retry times = 4, when reaches max retry times , it will log.error and don't compensate send
        compensateSender.send(4);
        
        compensateSender.sendByIdIgnoreStatus(1L);
        compensateSender.sendByMsgIdIgnoreStatus("xxx");
        
    }
```
