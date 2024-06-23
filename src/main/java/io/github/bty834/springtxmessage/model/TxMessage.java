package io.github.bty834.springtxmessage.model;

import io.github.bty834.springtxmessage.config.Note;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TxMessage {
    private String topic;
    private String shardingKey;
    @Nullable
    @Note("send success callbacks insert this field")
    private String msgId;
    @Note("json string")
    private String content;
}
