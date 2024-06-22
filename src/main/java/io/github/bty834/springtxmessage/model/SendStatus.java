package io.github.bty834.springtxmessage.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SendStatus {

    INIT(0),SUCCESS(100),FAILED(-2);

    private final int code;
}
