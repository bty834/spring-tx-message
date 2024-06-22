package io.github.bty834.springtxmessage.model;

import lombok.Data;

@Data
public class TxMessageSendResult {
    private boolean success;
    private String msgId;
}
