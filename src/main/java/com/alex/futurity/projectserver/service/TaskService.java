package com.alex.futurity.projectserver.service;

import com.alex.futurity.projectserver.dao.TaskDao;
import com.alex.futurity.projectserver.dto.ChangeTaskIndexRequestDto;
import com.alex.futurity.projectserver.dto.CreationTaskDto;
import com.alex.futurity.projectserver.dto.TaskDto;
import com.alex.futurity.projectserver.entity.ProjectColumn;
import com.alex.futurity.projectserver.entity.Task;
import com.alex.futurity.projectserver.exception.ClientSideException;
import com.alex.futurity.projectserver.message.TaskEventPublisher;
import com.alex.futurity.projectserver.model.UserProject;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class TaskService {
    private final ColumnService columnService;
    private final TaskEventPublisher eventPublisher;
    private final TaskDao taskDao;

    public long createTask(@NonNull CreationTaskDto creationTaskDto, @NonNull Long columnId) {
        Task task = columnService.addTaskToColumn(creationTaskDto, columnId);
        if (task.hasDeadline() && !task.isCompleted()) {
            eventPublisher.publishCreationEvent(task);
        }

        return task.getId();
    }

    public void deleteTask(long columnId, long taskId) {
        taskDao.deleteTask(columnId, taskId)
                .filter(task -> task.hasDeadline() && !task.isCompleted())
                .ifPresent(eventPublisher::publishDeleteEvent);
    }

    public TaskDto getTaskInfo(@NonNull Long taskId) {
        return taskDao.findTaskById(taskId)
                .map(TaskDto::fromTask)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Task with id %s not found", taskId)));
    }

    @Transactional
    public void changeTaskIndex(@NonNull UserProject userProject, @NonNull ChangeTaskIndexRequestDto request) {
        ProjectColumn columnFrom = columnService.getColumnByIndex(userProject, request.getFromColumn());
        ProjectColumn columnTo = columnService.getColumnByIndex(userProject, request.getToColumn());
        List<Task> tasksFrom = columnFrom.getTasks().stream().sorted(Comparator.comparingInt(Task::getIndex)).toList();
        List<Task> tasksTo = columnTo.getTasks().stream().sorted(Comparator.comparingInt(Task::getIndex)).toList();

        if (request.getFrom() > tasksFrom.size() || request.getTo() > tasksTo.size() + 1) {
            throw new ClientSideException("Tasks out of bounds", HttpStatus.BAD_REQUEST);
        }

        Task task = tasksFrom.get(request.getFrom());

        if (Objects.equals(request.getFromColumn(), request.getToColumn())) {
            task.setIndex(request.getTo());

            if (request.getFrom() < request.getTo()) {
                tasksFrom.stream()
                        .filter(t -> !Objects.equals(task.getId(), t.getId()))
                        .filter(t -> t.getIndex() >= request.getFrom() && t.getIndex() <= request.getTo())
                        .forEach(t -> t.setIndex(t.getIndex() - 1));
            } else {
                tasksFrom.stream()
                        .filter(t -> !Objects.equals(task.getId(), t.getId()))
                        .filter(t -> t.getIndex() >= request.getTo() && t.getIndex() <= request.getFrom())
                        .forEach(t -> t.setIndex(t.getIndex() + 1));
            }
        } else {
            tasksFrom.stream()
                    .filter(t -> t.getIndex() > request.getFrom())
                    .forEach(t -> t.setIndex(t.getIndex() - 1));
            task.setIndex(request.getTo())
                    .setColumn(columnTo)
                    .setCompleted(columnTo.isDoneColumn());
            publishTaskEvent(columnFrom, columnTo, task);

            tasksTo.stream()
                    .filter(t -> t.getIndex() >= request.getTo())
                    .forEach(t -> t.setIndex(t.getIndex() + 1));
        }
    }

    public void changeTaskName(@NonNull Long taskId, @NonNull String taskName) {
        taskDao.changeTaskName(taskId, taskName);
    }

    public void changeTaskDeadline(@NonNull Long taskId, ZonedDateTime deadline) {
        taskDao.changeTaskDeadline(taskId, deadline)
                .filter(task -> !task.isCompleted())
                .ifPresent(eventPublisher::publishUpdateEvent);
    }

    private void publishTaskEvent(ProjectColumn columnFrom, ProjectColumn columnTo, Task task) {
        if (task.hasDeadline() && (columnFrom.isDoneColumn() || columnTo.isDoneColumn())) {
            eventPublisher.publishUpdateEvent(task);
        }
    }
}
