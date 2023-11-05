package com.alex.futurity.projectserver.dto;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ChangeTaskDeadlineDto {
    private ZonedDateTime deadline;
}
