package com.alex.futurity.projectserver.dto;

import com.alex.futurity.projectserver.entity.ProjectColumn;
import com.alex.futurity.projectserver.entity.Task;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class ProjectColumnDto {
    private long id;
    private String name;
    private List<TaskDto> tasks;
    @JsonProperty("isDone")
    private boolean done;

    public static ProjectColumnDto fromProjectColumn(ProjectColumn projectColumn) {
        List<TaskDto> taskDtos = projectColumn.getTasks().stream()
                .sorted(Comparator.comparingInt(Task::getIndex))
                .map(TaskDto::fromTask)
                .collect(Collectors.toList());

        return new ProjectColumnDto(projectColumn.getId(), projectColumn.getName(), taskDtos, projectColumn.isDoneColumn());
    }
}
