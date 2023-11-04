package com.alex.futurity.projectserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestStringDto {
    @NotEmpty(message = "Value must not be null")
    @NotBlank(message = "Value must not be null")
    @NotNull(message = "Value must not be null")
    private String value;
}
