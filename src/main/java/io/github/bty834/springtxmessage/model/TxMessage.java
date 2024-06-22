package io.github.bty834.springtxmessage.model;

import io.github.bty834.springtxmessage.config.Note;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.lang.Nullable;

@Data
public class TxMessage {
    private Long id;
    private String topic;
    private String key;
    @Nullable
    @Note("send success callbacks insert this field")
    private String msgId;
    private SendStatus sendStatus;
    private String content;
    private Integer retryTimes;
    @Note("send failed set nextRetryTime")
    private LocalDate nextRetryTime;
    private Boolean deleted;
    private LocalDate createTime;
    private LocalDate updateTime;
}
