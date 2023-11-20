package com.alex.futurity.projectserver.dao;

import com.alex.futurity.projectserver.entity.Task;
import com.alex.futurity.projectserver.repo.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class TaskDao {
    private final TaskRepository taskRepo;

    public Optional<Task> findTaskById(@NonNull Long taskId) {
        return taskRepo.findById(taskId);
    }

    @Transactional
    public Optional<Task> deleteTask(@NonNull Long columnId, @NonNull Long taskId) {
        List<Task> tasks = taskRepo.findAllByColumnId(columnId);

        return tasks.stream()
                .filter(task -> task.getId().equals(taskId))
                .findFirst()
                .map(task -> deleteAndShift(task, tasks));
    }

    @Transactional
    public void changeTaskName(@NonNull Long taskId, @NonNull String taskName) {
        taskRepo.findById(taskId)
                .ifPresent(task -> task.setName(taskName));
    }

    @Transactional
    public Optional<Task> changeTaskDeadline(@NonNull Long taskId, ZonedDateTime deadline) {
        return taskRepo.findById(taskId)
                .map(task -> task.setDeadline(deadline));
    }

    private Task deleteAndShift(Task taskToDelete, List<Task> tasks) {
        taskRepo.delete(taskToDelete);

        tasks.stream()
                .filter(task -> task.getIndex() > taskToDelete.getIndex())
                .forEach(task -> task.setIndex(task.getIndex() - 1));

        return taskToDelete;
    }
}
