package io.github.bty834.springtxmessage.support;

import io.github.bty834.springtxmessage.TxMessageSendAdapter;
import io.github.bty834.springtxmessage.TxMessageSender;
import io.github.bty834.springtxmessage.model.SendStatus;
import io.github.bty834.springtxmessage.model.TxMessage;
import io.github.bty834.springtxmessage.model.TxMessagePO;
import io.github.bty834.springtxmessage.model.TxMessageSendResult;
import io.github.bty834.springtxmessage.utils.TransactionUtil;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
        if (messages.isEmpty()) {
            return;
        }
        doBatchSave(messages);
    }

    private List<TxMessagePO> doBatchSave(List<TxMessage> messages) {
        List<TxMessagePO> txMessagePOS = messages.stream().map(
            msg -> {
                TxMessagePO txMessagePO = TxMessagePO.convertFrom(msg);
                txMessagePO.setSendStatus(SendStatus.INIT);
                txMessagePO.setRetryTimes(0);
                Integer intervalSec = propertyResolver.getProperty(COMPENSATE_INTERVAL_SECONDS, Integer.class, 10);
                LocalDateTime nextRetryTime = LocalDateTime.now().plusSeconds(intervalSec);
                txMessagePO.setNextRetryTime(nextRetryTime);
                return txMessagePO;
            }
        ).collect(Collectors.toList());
        txMessageRepository.batchSave(txMessagePOS);
        return txMessagePOS;
    }

    @Override
    public void saveAndTrySend(TxMessage message) {
        assert message != null;
        if (!propertyResolver.getProperty(ENABLED_KEY, Boolean.class, Boolean.FALSE)) {
            return;
        }
        List<TxMessagePO> txMessagePOS = doBatchSave(Collections.singletonList(message));
        TransactionUtil.executeAfterCommit(()-> trySend(txMessagePOS), (e)->{});
    }

    @Override
    public void batchSaveAndTrySend(List<TxMessage> messages) {
        assert messages != null;
        if (!propertyResolver.getProperty(ENABLED_KEY, Boolean.class, Boolean.FALSE)) {
            return;
        }
        List<TxMessagePO> txMessagePOS = doBatchSave(messages);
        TransactionUtil.executeAfterCommit(()-> trySend(txMessagePOS), (e)->{});
    }

    private void trySend(List<TxMessagePO> messages) {
        messages.forEach(msg -> {
            try {
                TxMessageSendResult sendResult = txMessageSendAdapter.send(msg.convertToTxMessage());
                if (!sendResult.isSuccess()) {
                    throw new RuntimeException("Tx message send failed: " + msg);
                }
                assert sendResult.getMsgId() != null;
                msg.setMsgId(sendResult.getMsgId());

                txMessageRepository.updateById(msg);
            } catch (Exception e) {
                log.error("trySend tx message failed :{}", msg, e);
                Integer interval = propertyResolver.getProperty(COMPENSATE_INTERVAL_SECONDS, Integer.class, 10);
                LocalDateTime nextRetryTime = LocalDateTime.now().plusSeconds(interval);
                msg.setNextRetryTime(nextRetryTime);
                msg.setSendStatus(SendStatus.FAILED);

                txMessageRepository.updateById2(msg);
            }
        });
    }

}
