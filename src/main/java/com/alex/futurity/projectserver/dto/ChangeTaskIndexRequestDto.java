package com.alex.futurity.projectserver.dto;
import lombok.Data;

@Data
public class ChangeTaskIndexRequestDto {
    private int fromColumn;
    private int toColumn;
    private int from;
    private int to;
}
