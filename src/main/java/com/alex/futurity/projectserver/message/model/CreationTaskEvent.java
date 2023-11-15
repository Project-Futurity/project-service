package com.alex.futurity.projectserver.message.model;


import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;

@Getter
@SuperBuilder
public class CreationTaskEvent extends TaskEvent {
    @NonNull
    private final ZonedDateTime deadline;
    @NonNull
    private final Long userId;
}
