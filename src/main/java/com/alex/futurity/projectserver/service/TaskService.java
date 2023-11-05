package com.alex.futurity.projectserver.service;

import com.alex.futurity.projectserver.dto.ChangeTaskIndexRequestDto;
import com.alex.futurity.projectserver.dto.CreationTaskDto;
import com.alex.futurity.projectserver.entity.ProjectColumn;
import com.alex.futurity.projectserver.entity.Task;
import com.alex.futurity.projectserver.exception.ClientSideException;
import com.alex.futurity.projectserver.model.UserProject;
import com.alex.futurity.projectserver.repo.TaskRepository;
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
    private final TaskRepository taskRepo;

    public long createTask(@NonNull CreationTaskDto creationTaskDto, @NonNull Long columnId) {
        return columnService.addTaskToColumn(creationTaskDto, columnId).getId();
    }

    @Transactional
    public void deleteTask(long columnId, long taskId) {
        List<Task> tasks = taskRepo.findAllByColumnId(columnId);

        tasks.stream()
                .filter(task -> task.getId().equals(taskId))
                .findFirst()
                .ifPresent(task -> deleteAndShift(task, tasks));
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
            task.setIndex(request.getTo());
            task.setColumn(columnTo);

            tasksTo.stream()
                    .filter(t -> t.getIndex() >= request.getTo())
                    .forEach(t -> t.setIndex(t.getIndex() + 1));
        }
    }


    @Transactional
    public void changeTaskName(@NonNull Long taskId, @NonNull String taskName) {
        taskRepo.findById(taskId)
                        .ifPresent(task -> task.setName(taskName));
    }


    @Transactional
    public void changeTaskDeadline(@NonNull Long taskId, ZonedDateTime deadline) {
        taskRepo.findById(taskId)
                .ifPresent(task -> task.setDeadline(deadline));
    }

    private void deleteAndShift(Task taskToDelete, List<Task> tasks) {
        taskRepo.delete(taskToDelete);

        tasks.stream()
                .filter(task -> task.getIndex() > taskToDelete.getIndex())
                .forEach(task -> task.setIndex(task.getIndex() - 1));
    }
}
