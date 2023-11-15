package com.alex.futurity.projectserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
@ToString
public class CreationTaskDto {
    @NotEmpty(message = "Name must not be null")
    @NotBlank(message = "Name must not be null")
    @NotNull(message = "Name must not be null")
    private String name;
    private ZonedDateTime deadline;
}
