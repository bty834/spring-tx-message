package io.github.bty834.springtxmessage.support;

import io.github.bty834.springtxmessage.TxMessageCompensateSender;
import io.github.bty834.springtxmessage.TxMessageSendAdapter;
import io.github.bty834.springtxmessage.model.SendStatus;
import io.github.bty834.springtxmessage.model.TxMessage;
import io.github.bty834.springtxmessage.model.TxMessageSendResult;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.PropertyResolver;

@Slf4j
@RequiredArgsConstructor
public class DefaultTxMessageCompensateSender implements TxMessageCompensateSender {

    private final TxMessageSendAdapter txMessageSendAdapter;

    private final TxMessageRepository txMessageRepository;

    private final PropertyResolver propertyResolver;

    @Override
    public void send(int maxRetryTimes) {
        if (!propertyResolver.getProperty(COMPENSATE_ENABLED_KEY, Boolean.class, Boolean.FALSE)) {
            return;
        }
        Integer delaySeconds = propertyResolver.getProperty(COMPENSATE_INTERVAL_SECONDS, Integer.class, 6);
        List<TxMessage> txMessages = txMessageRepository.queryReadyToSendMessages(maxRetryTimes, delaySeconds);
        doSend(txMessages);
    }

    public void sendByIdIgnoreStatus(Long id) {
        if (!propertyResolver.getProperty(COMPENSATE_ENABLED_KEY, Boolean.class, Boolean.FALSE)) {
            return;
        }
        TxMessage txMessage = txMessageRepository.queryById(id);
        doSend(Collections.singletonList(txMessage));
    }

    public void sendByMsgIdIgnoreStatus(String msgId) {
        if (!propertyResolver.getProperty(COMPENSATE_ENABLED_KEY, Boolean.class, Boolean.FALSE)) {
            return;
        }
        TxMessage txMessage = txMessageRepository.queryByMsgId(msgId);
        doSend(Collections.singletonList(txMessage));
    }

    private void doSend(List<TxMessage> txMessages){
        txMessages.forEach(msg -> {
            try {
                TxMessageSendResult sendResult = txMessageSendAdapter.send(msg);
                if (!sendResult.isSuccess()) {
                    throw new RuntimeException("Tx message compensate send failed: " + msg);
                }
                assert sendResult.getMsgId() != null;
                msg.setMsgId(sendResult.getMsgId());
                txMessageRepository.updateToSuccess(msg);
                msg.setSendStatus(SendStatus.SUCCESS);
            } catch (Exception e) {
                log.error("compensate tx message failed :{}", msg, e);
                Integer interval = propertyResolver.getProperty(COMPENSATE_INTERVAL_SECONDS, Integer.class, 10);
                LocalDate nextRetryTime = LocalDate.now().plus(Duration.of(interval, ChronoUnit.SECONDS));
                msg.setNextRetryTime(nextRetryTime);
                msg.setRetryTimes(msg.getRetryTimes() + 1);
                txMessageRepository.updateToFailed(msg);
                msg.setSendStatus(SendStatus.FAILED);
            }
        });
    }
}
