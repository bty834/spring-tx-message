package io.github.bty834.springtxmessage;

public interface TxMessageCompensateSender {

    String COMPENSATE_ENABLED_KEY = "spring.tx.message.compensate.send.enabled";
    String MAX_RETRY_TIMES_KEY = "spring.tx.message.max.retry.times";
    String COMPENSATE_INTERVAL_SECONDS = "spring.tx.message.compensate.interval.seconds";

    void send();

    void sendByIdIgnoreStatus(Long id);

    void sendByMsgIdIgnoreStatus(String msgId);
}
