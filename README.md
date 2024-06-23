

# Quick Start

1. add dependency

```xml
   <dependency>
            <groupId>io.github.bty834</groupId>
            <artifactId>spring-tx-message</artifactId>
            <version>0.0.1-SNAPSHOT</version>
   </dependency>
```

2. configure adapter and repository

```java
@Configuration
public class TxMessageConfig {
    @Bean
    public TxMessageSendAdapter txMessageSendAdapter() {
        return new MyMessageSendAdapter();
    }
    @Bean
    public TxMessageRepository txMessageRepository(DataSource dataSource) {
        return new TxMessageRepository(dataSource, "local_message");
    }
}

/**
 * your send adapter
 */
class MyMessageSendAdapter implements TxMessageSendAdapter {

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
        // send with retry times = 4
        compensateSender.send(4);
        
        compensateSender.sendByIdIgnoreStatus(1L);
        compensateSender.sendByMsgIdIgnoreStatus("xxx");
        
    }
```
