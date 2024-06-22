package io.github.bty834.springtxmessage.support;

import io.github.bty834.springtxmessage.TxMessageSendAdapter;
import io.github.bty834.springtxmessage.TxMessageSender;
import io.github.bty834.springtxmessage.model.TxMessage;
import io.github.bty834.springtxmessage.model.TxMessageSendResult;
import io.github.bty834.springtxmessage.utils.TransactionUtil;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.PropertyResolver;

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
                txMessageRepository.updateToSuccess(msg, sendResult);
            } catch (Exception e) {
                log.error("trySend tx message failed :{}", msg, e);
                txMessageRepository.updateToFailed(msg);
            }
        });
    }

}
