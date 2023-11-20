package com.alex.futurity.projectserver.context;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class User {
    private final Long userId;
    private final boolean hasTelegram;
}
