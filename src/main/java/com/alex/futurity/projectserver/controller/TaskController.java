package com.alex.futurity.projectserver.controller;

import com.alex.futurity.projectserver.dto.ChangeTaskDeadlineDto;
import com.alex.futurity.projectserver.dto.ChangeTaskIndexRequestDto;
import com.alex.futurity.projectserver.dto.CreationTaskDto;
import com.alex.futurity.projectserver.dto.RequestStringDto;
import com.alex.futurity.projectserver.service.TaskService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.ZonedDateTime;

@RestController
@AllArgsConstructor
@Log4j2
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/{userId}/task/{projectId}/{columnId}/create")
    @ResponseStatus(HttpStatus.CREATED)
    public long createTask(@PathVariable long userId, @PathVariable long projectId, @PathVariable long columnId,
                           @Valid @RequestBody CreationTaskDto creationTaskDto) {
        log.info("Handling creation task request. User id: {}, project id: {}, column id: {}, data: {}",
                userId, projectId, columnId, creationTaskDto);

        return taskService.createTask(userId, projectId, columnId, creationTaskDto);
    }

    @DeleteMapping("/{userId}/task/{projectId}/{columnId}/{taskId}/delete")
    @ResponseStatus(HttpStatus.OK)
    public void deleteTask(@PathVariable long userId, @PathVariable long projectId, @PathVariable long columnId,
                           @PathVariable long taskId) {
        log.info("Handling deleting task request. User id: {}, project id: {}, column id: {}, task id: {}",
                userId, projectId, columnId, taskId);

        taskService.deleteTask(userId, projectId, columnId, taskId);
    }

    @PatchMapping("/{userId}/task/{projectId}/index/change")
    @ResponseStatus(HttpStatus.OK)
    public void changeTaskIndex(@PathVariable long userId, @PathVariable long projectId,
                                @RequestBody ChangeTaskIndexRequestDto request) {
        log.info("Handling changing task index request. User id: {}, project id: {}, Data: {}",
                projectId, userId, request);

        taskService.changeTaskIndex(userId, projectId, request);
    }

    @PatchMapping("/{userId}/task/{projectId}/{columnId}/{taskId}/name")
    @ResponseStatus(HttpStatus.OK)
    public void changeTaskName(@PathVariable long userId, @PathVariable long projectId, @PathVariable long columnId,
                               @PathVariable long taskId, @Valid @RequestBody RequestStringDto taskName) {
        log.info("Handling changing task name request. User id: {}, project id: {}, column index: {}, task index: {}, task name: {}",
                userId, projectId, columnId, taskId, taskName.getValue());

        taskService.changeTaskName(userId, projectId, columnId, taskId, taskName.getValue());
    }

    @PatchMapping("/{userId}/task/{projectId}/{columnId}/{taskId}/deadline")
    @ResponseStatus(HttpStatus.OK)
    public void changeTaskDeadline(@PathVariable long userId,
                                   @PathVariable long projectId,
                                   @PathVariable long columnId,
                                   @PathVariable long taskId,
                                   @RequestBody ChangeTaskDeadlineDto deadlineDto) {
        log.info("Handling changing task deadline request. User id: {}, project id: {}, column index: {}, task index: {}, task deadline: {}",
                userId, projectId, columnId, taskId, deadlineDto.getDeadline());

        taskService.changeTaskDeadline(userId, projectId, columnId, taskId, deadlineDto.getDeadline());
    }
}
