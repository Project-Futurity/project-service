package com.alex.futurity.projectserver.controller;

import com.alex.futurity.projectserver.dto.TaskDto;
import com.alex.futurity.projectserver.service.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/task/info")
public class TaskInfoController {
    private final TaskService taskService;

    @GetMapping("/{id}")
    public TaskDto getTaskInfo(@PathVariable("id") Long taskId) {
        return taskService.getTaskInfo(taskId);
    }
}
