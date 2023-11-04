package com.alex.futurity.projectserver.dto;

import com.alex.futurity.projectserver.entity.Task;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
public class TaskDto {
    private long id;
    private String name;
    private long columnId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private ZonedDateTime deadline;

    public static TaskDto fromTask(Task task) {
        return new TaskDto(task.getId(), task.getName(), task.getColumn().getId(), task.getDeadline());
    }
}
