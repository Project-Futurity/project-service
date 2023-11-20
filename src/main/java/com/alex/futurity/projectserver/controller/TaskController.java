package com.alex.futurity.projectserver.controller;

import com.alex.futurity.projectserver.context.UserContext;
import com.alex.futurity.projectserver.dto.ChangeTaskDeadlineDto;
import com.alex.futurity.projectserver.dto.ChangeTaskIndexRequestDto;
import com.alex.futurity.projectserver.dto.CreationTaskDto;
import com.alex.futurity.projectserver.dto.RequestStringDto;
import com.alex.futurity.projectserver.model.UserProject;
import com.alex.futurity.projectserver.service.TaskService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j

@RestController
@AllArgsConstructor
@RequestMapping("/task/{projectId}")
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/{columnId}/create")
    @ResponseStatus(HttpStatus.CREATED)
    public long createTask(@PathVariable long projectId, @PathVariable long columnId,
                           @Valid @RequestBody CreationTaskDto creationTaskDto) {
        log.info("Handling creation task request. project id: {}, column id: {}, data: {}",
                projectId, columnId, creationTaskDto);

        return taskService.createTask(creationTaskDto, columnId);
    }

    @DeleteMapping("/{columnId}/{taskId}/delete")
    @ResponseStatus(HttpStatus.OK)
    public void deleteTask(@PathVariable long projectId, @PathVariable long columnId,
                           @PathVariable long taskId) {
        log.info("Handling deleting task request. User id: {}, project id: {}, column id: {}, task id: {}",
                UserContext.getUserId(), projectId, columnId, taskId);

        taskService.deleteTask(columnId, taskId);
    }

    @PatchMapping("/index/change")
    @ResponseStatus(HttpStatus.OK)
    public void changeTaskIndex(@PathVariable long projectId,
                                @RequestBody ChangeTaskIndexRequestDto request) {
        log.info("Handling changing task index request. User id: {}, project id: {}, Data: {}",
                projectId, UserContext.getUserId(), request);

        taskService.changeTaskIndex(UserProject.of(UserContext.getUserId(), projectId), request);
    }

    @PatchMapping("/{columnId}/{taskId}/name")
    @ResponseStatus(HttpStatus.OK)
    public void changeTaskName(@PathVariable long projectId, @PathVariable long columnId,
                               @PathVariable long taskId, @Valid @RequestBody RequestStringDto taskName) {
        log.info("Handling changing task name request. User id: {}, project id: {}, column index: {}, task index: {}, task name: {}",
                UserContext.getUserId(), projectId, columnId, taskId, taskName.getValue());

        taskService.changeTaskName(taskId, taskName.getValue());
    }

    @PatchMapping("/{columnId}/{taskId}/deadline")
    @ResponseStatus(HttpStatus.OK)
    public void changeTaskDeadline(@PathVariable long projectId,
                                   @PathVariable long columnId,
                                   @PathVariable long taskId,
                                   @RequestBody ChangeTaskDeadlineDto deadlineDto) {
        log.info("Handling changing task deadline request. User id: {}, project id: {}, column index: {}, task index: {}, task deadline: {}",
                UserContext.getUserId(), projectId, columnId, taskId, deadlineDto.getDeadline());

        taskService.changeTaskDeadline(taskId, deadlineDto.getDeadline());
    }
}
