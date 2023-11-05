package com.alex.futurity.projectserver.dto;
import lombok.NonNull;
import lombok.Value;

@Value
public class ChangeTaskIndexRequestDto {
    @NonNull Integer fromColumn;
    @NonNull Integer toColumn;
    @NonNull Integer from;
    @NonNull Integer to;
}
