package com.alex.futurity.projectserver.message.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum TaskRoutingKey {
    CREATED("created"),
    UPDATED("updated"),
    DELETED("deleted");

    @Getter
    private final String routingKey;

    public static final String HEADER_NAME = "type";
}
