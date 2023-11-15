package com.alex.futurity.projectserver.message.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;

@Getter
@SuperBuilder
public class UpdateTaskEvent extends TaskEvent {
    private final ZonedDateTime deadline;
    @NonNull
    private final Boolean completed;
    @NonNull
    private final Long userId;
}
