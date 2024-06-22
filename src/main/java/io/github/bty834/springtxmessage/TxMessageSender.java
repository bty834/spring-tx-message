package io.github.bty834.springtxmessage;

import io.github.bty834.springtxmessage.model.TxMessage;
import java.util.List;

public interface TxMessageSender {

    String ENABLED_KEY = "spring.tx.message.send.enabled";

    void batchSave(List<TxMessage> messages);

    void saveAndTrySend(TxMessage message);

    void batchSaveAndTrySend(List<TxMessage> messages);
}
