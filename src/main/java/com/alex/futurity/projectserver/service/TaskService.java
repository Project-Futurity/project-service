package com.alex.futurity.projectserver.service;

import com.alex.futurity.projectserver.dto.ChangeTaskIndexRequestDto;
import com.alex.futurity.projectserver.dto.CreationTaskDto;
import com.alex.futurity.projectserver.entity.Project;
import com.alex.futurity.projectserver.entity.ProjectColumn;
import com.alex.futurity.projectserver.entity.Task;
import com.alex.futurity.projectserver.exception.ClientSideException;
import com.alex.futurity.projectserver.repo.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class TaskService {
    private final ColumnService columnService;
    private final TaskRepository taskRepo;
    private static final String NOT_FOUND_MESSAGE = "The task is associated with such data does not exist";


    public long createTask(long userId, long projectId, long columnId, CreationTaskDto creationTaskDto) {
        Task task = columnService.addTaskToColumn(userId, projectId, columnId, creationTaskDto);
        return task.getId();
    }


    @Transactional
    public void deleteTask(long userId, long projectId, long columnId, long taskId) {
        List<Task> tasks = columnService.getTasksFromColumn(userId, projectId, columnId);
        Task taskToRemove = tasks.stream().filter(task -> task.getId().equals(taskId)).findFirst()
                .orElseThrow(() -> new ClientSideException(NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));

        tasks.stream()
                .filter(task -> task.getIndex() > taskToRemove.getIndex())
                .forEach(task -> task.setIndex(task.getIndex() - 1));

        taskRepo.deleteTask(taskToRemove.getId());
    }


    @Transactional
    public void changeTaskIndex(long userId, long projectId, ChangeTaskIndexRequestDto request) {
        List<Task> columnFrom = taskRepo.findAllTasks(userId, projectId, request.getFromColumn());
        List<Task> columnTo = taskRepo.findAllTasks(userId, projectId, request.getToColumn());

        if (request.getFrom() < 0 || request.getTo() < 0
                || (request.getFromColumn() == request.getToColumn() && request.getFrom() == request.getTo())
                || request.getFrom() > columnFrom.size() || request.getTo() > columnTo.size() + 1) {
            throw new ClientSideException("Tasks out of bounds", HttpStatus.BAD_REQUEST);
        }

        Task task = columnFrom.get(request.getFrom());

        if (request.getFromColumn() == request.getToColumn()) {
            task.setIndex(request.getTo());

            if (request.getFrom() < request.getTo()) {
                columnFrom.stream()
                        .filter(t -> t.getIndex() >= request.getFrom() && t.getIndex() <= request.getTo()
                                && !Objects.equals(task.getId(), t.getId()))
                        .forEach(t -> t.setIndex(t.getIndex() - 1));
            } else {
                columnFrom.stream()
                        .filter(t -> t.getIndex() >= request.getTo() && t.getIndex() <= request.getFrom()
                                && !Objects.equals(task.getId(), t.getId()))
                        .forEach(t -> t.setIndex(t.getIndex() + 1));
            }
        } else {
            columnFrom.stream()
                    .filter(t -> t.getIndex() > request.getFrom())
                    .forEach(t -> t.setIndex(t.getIndex() - 1));
            task.setIndex(request.getTo());

            taskRepo.changeColumnId(userId, projectId, request.getToColumn(), task.getId());

            columnTo.stream()
                    .filter(t -> t.getIndex() >= request.getTo())
                    .forEach(t -> t.setIndex(t.getIndex() + 1));
        }
    }


    @Transactional
    public void changeTaskName(long userId, long projectId, long columnId, long taskId, String taskName) {
        Task task = findTaskByTaskId(userId, projectId, columnId, taskId);
        task.setName(taskName);
    }


    public void changeTaskDeadline(long userId, long projectId, long columnId, long taskId, ZonedDateTime deadline) {
        Task task = findTaskByTaskId(userId, projectId, columnId, taskId);
        task.setDeadline(deadline);
    }

    private Task findTaskByTaskId(long userId, long projectId, long columnId, long taskId) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new ClientSideException(NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));
        ProjectColumn column = task.getColumn();
        Project project = column.getProject();

        if (column.getId() != columnId || project.getUserId() != userId || project.getId() != projectId) {
            throw new ClientSideException(NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND);
        }

        return task;
    }
}
