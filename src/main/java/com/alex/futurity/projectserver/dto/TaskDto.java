package com.alex.futurity.projectserver.dto;

import com.alex.futurity.projectserver.entity.ProjectColumn;
import com.alex.futurity.projectserver.entity.Task;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
@Builder
public class TaskDto {
    private long id;
    private String name;
    private long columnId;
    private long projectId;
    private ZonedDateTime deadline;

    public static TaskDto fromTask(Task task) {
        ProjectColumn column = task.getColumn();

        return TaskDto.builder()
                .id(task.getId())
                .name(task.getName())
                .columnId(column.getId())
                .projectId(column.getProject().getId())
                .deadline(task.getDeadline())
                .build();
    }
}
