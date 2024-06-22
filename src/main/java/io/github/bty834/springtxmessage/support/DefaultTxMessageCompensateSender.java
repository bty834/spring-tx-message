package io.github.bty834.springtxmessage.support;

import io.github.bty834.springtxmessage.TxMessageCompensateSender;
import io.github.bty834.springtxmessage.TxMessageSendAdapter;
import io.github.bty834.springtxmessage.model.TxMessage;
import io.github.bty834.springtxmessage.model.TxMessageSendResult;
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
    public void send() {
        Integer maxRetryTimes = propertyResolver.getProperty(MAX_RETRY_TIMES_KEY, Integer.class, 3);
        Integer delaySeconds = propertyResolver.getProperty(COMPENSATE_INTERVAL_SECONDS, Integer.class, 6);

        List<TxMessage> txMessages = txMessageRepository.queryReadyToSendMessages(maxRetryTimes, delaySeconds);
        doSend(txMessages);
    }

    public void sendByIdIgnoreStatus(Long id) {
        TxMessage txMessage = txMessageRepository.queryById(id);
        doSend(Collections.singletonList(txMessage));
    }

    public void sendByMsgIdIgnoreStatus(String msgId) {
        TxMessage txMessage = txMessageRepository.queryByMsgId(msgId);
        doSend(Collections.singletonList(txMessage));
    }

    private void doSend(List<TxMessage> txMessages){
        txMessages.forEach(msg -> {
            try {
                TxMessageSendResult sendResult = txMessageSendAdapter.send(msg);
                txMessageRepository.updateToSuccess(msg, sendResult);
            } catch (Exception e) {
                log.error("send tx message failed :{}",msg, e);
                txMessageRepository.updateToFailed(msg);
            }
        });
    }
}
