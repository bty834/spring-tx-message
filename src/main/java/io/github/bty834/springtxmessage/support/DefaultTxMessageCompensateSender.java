package io.github.bty834.springtxmessage.support;

import io.github.bty834.springtxmessage.TxMessageCompensateSender;
import io.github.bty834.springtxmessage.TxMessageSendAdapter;
import io.github.bty834.springtxmessage.model.SendStatus;
import io.github.bty834.springtxmessage.model.TxMessagePO;
import io.github.bty834.springtxmessage.model.TxMessageSendResult;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
        List<TxMessagePO> txMessages = txMessageRepository.queryReadyToSendMessages(maxRetryTimes, delaySeconds);
        List<Long> ids = txMessages.stream()
                                 .filter(msg -> msg.getRetryTimes() >= maxRetryTimes)
                                 .map(TxMessagePO::getId)
                                 .collect(Collectors.toList());
        if (!ids.isEmpty()) {
            log.error("reaches max retry times {}, ids: {}", maxRetryTimes, ids);
        }
        txMessages.removeIf(msg -> msg.getRetryTimes() >= maxRetryTimes);
        doSend(txMessages);
    }

    public void sendByIdIgnoreStatus(Long id) {
        if (!propertyResolver.getProperty(COMPENSATE_ENABLED_KEY, Boolean.class, Boolean.FALSE)) {
            return;
        }
        TxMessagePO txMessage = txMessageRepository.queryById(id);
        doSend(Collections.singletonList(txMessage));
    }

    public void sendByMsgIdIgnoreStatus(String msgId) {
        if (!propertyResolver.getProperty(COMPENSATE_ENABLED_KEY, Boolean.class, Boolean.FALSE)) {
            return;
        }
        TxMessagePO txMessage = txMessageRepository.queryByMsgId(msgId);
        doSend(Collections.singletonList(txMessage));
    }

    private void doSend(List<TxMessagePO> txMessages){
        txMessages.forEach(msg -> {
            try {
                TxMessageSendResult sendResult = txMessageSendAdapter.send(msg.convertToTxMessage());
                if (!sendResult.isSuccess()) {
                    throw new RuntimeException("Tx message compensate send failed: " + msg);
                }
                assert sendResult.getMsgId() != null;
                msg.setMsgId(sendResult.getMsgId());
                msg.setSendStatus(SendStatus.SUCCESS);
                txMessageRepository.updateById(msg);
            } catch (Exception e) {
                log.error("compensate tx message failed :{}", msg, e);
                Integer interval = propertyResolver.getProperty(COMPENSATE_INTERVAL_SECONDS, Integer.class, 10);
                LocalDateTime nextRetryTime = LocalDateTime.now().plusSeconds(interval);
                msg.setNextRetryTime(nextRetryTime);
                msg.setRetryTimes(msg.getRetryTimes() + 1);
                msg.setSendStatus(SendStatus.FAILED);
                txMessageRepository.updateById2(msg);
            }
        });
    }
}
