package io.github.bty834.springtxmessage;

import io.github.bty834.springtxmessage.config.Note;
import io.github.bty834.springtxmessage.model.TxMessage;
import io.github.bty834.springtxmessage.model.TxMessageSendResult;

public interface TxMessageSendAdapter {

    @Note("do not catch your mq implementation's sending error")
    TxMessageSendResult send(TxMessage message);
}
