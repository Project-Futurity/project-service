package com.alex.futurity.projectserver.message.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class TaskEvent {
    @NonNull
    private final Long id;
}
