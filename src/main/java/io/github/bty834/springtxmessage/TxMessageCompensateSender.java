package io.github.bty834.springtxmessage;

public interface TxMessageCompensateSender {

    String COMPENSATE_ENABLED_KEY = "spring.tx.message.compensate.send.enabled";
    String COMPENSATE_INTERVAL_SECONDS = "spring.tx.message.compensate.interval.seconds";

    void send(int maxRetryTimes);

    void sendByNumberIgnoreStatus(Long number);

    void sendByMsgIdIgnoreStatus(String msgId);
}
