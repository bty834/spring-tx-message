package io.github.bty834.springtxmessage.support;

import io.github.bty834.springtxmessage.TxMessageSendAdapter;
import io.github.bty834.springtxmessage.TxMessageSender;
import io.github.bty834.springtxmessage.model.SendStatus;
import io.github.bty834.springtxmessage.model.TxMessage;
import io.github.bty834.springtxmessage.model.TxMessageSendResult;
import io.github.bty834.springtxmessage.utils.TransactionUtil;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.PropertyResolver;

import static io.github.bty834.springtxmessage.TxMessageCompensateSender.COMPENSATE_INTERVAL_SECONDS;

@Slf4j
@RequiredArgsConstructor
public class DefaultTxMessageSender implements TxMessageSender {


    private final TxMessageSendAdapter txMessageSendAdapter;

    private final TxMessageRepository txMessageRepository;

    private final PropertyResolver propertyResolver;


    @Override
    public void batchSave(List<TxMessage> messages) {
        assert messages != null;
        if (!propertyResolver.getProperty(ENABLED_KEY, Boolean.class, Boolean.FALSE)) {
            return;
        }
        txMessageRepository.batchSave(messages);
    }

    @Override
    public void saveAndTrySend(TxMessage message) {
        assert message != null;

        batchSave(Collections.singletonList(message));
        TransactionUtil.executeAfterCommit(()-> trySend(Collections.singletonList(message)), (e)->{});
    }

    @Override
    public void batchSaveAndTrySend(List<TxMessage> messages) {
        assert messages != null;

        batchSave(messages);
        TransactionUtil.executeAfterCommit(()-> trySend(messages), (e)->{});
    }

    private void trySend(List<TxMessage> messages) {
        messages.forEach(msg -> {
            try {
                TxMessageSendResult sendResult = txMessageSendAdapter.send(msg);
                if (!sendResult.isSuccess()) {
                    throw new RuntimeException("Tx message send failed: " + msg);
                }
                assert sendResult.getMsgId() != null;
                msg.setMsgId(sendResult.getMsgId());
                txMessageRepository.updateToSuccess(msg);
                msg.setSendStatus(SendStatus.SUCCESS);
            } catch (Exception e) {
                log.error("trySend tx message failed :{}", msg, e);
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
