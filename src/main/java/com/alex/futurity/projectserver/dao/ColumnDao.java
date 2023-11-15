package com.alex.futurity.projectserver.dao;

import com.alex.futurity.projectserver.dto.CreationTaskDto;
import com.alex.futurity.projectserver.dto.ProjectColumnDto;
import com.alex.futurity.projectserver.entity.ProjectColumn;
import com.alex.futurity.projectserver.entity.Task;
import com.alex.futurity.projectserver.exception.ClientSideException;
import com.alex.futurity.projectserver.model.UserProject;
import com.alex.futurity.projectserver.repo.ColumnRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
@AllArgsConstructor
public class ColumnDao {
    private final ColumnRepository columnRepo;
    private static final String NOT_FOUND_MESSAGE = "The column is associated with such data does not exist: %s";


    public List<ProjectColumnDto> getColumns(@NonNull UserProject project) {
        return getColumnsByProject(project)
                .stream()
                .map(ProjectColumnDto::fromProjectColumn)
                .toList();
    }

    public List<ProjectColumn> getColumnsByProject(UserProject project) {
        return columnRepo.findAllByProject(project.getUserId(), project.getProjectId());
    }

    @Transactional
    public Optional<ProjectColumn> deleteColumn(@NonNull UserProject project, @NonNull Long columnId) {
        List<ProjectColumn> columns = getColumnsByProject(project);

        return columns.stream()
                .filter(column -> Objects.equals(column.getId(), columnId))
                .findFirst()
                .map(column -> deleteAndShift(column, columns));
    }

    @Transactional
    public Task addTaskToColumn(@NonNull CreationTaskDto creationTaskDto, @NonNull Long columnId) {
        return columnRepo.findById(columnId)
                .map(column -> column.addTask(new Task(creationTaskDto.getName(), creationTaskDto.getDeadline(), column)))
                .orElseThrow(() -> buildException(columnId));
    }

    @Transactional
    public void changeColumnName(@NonNull Long columnId, String columnName) {
        columnRepo.findById(columnId)
                .ifPresent(column -> column.setName(columnName));
    }

    @Transactional
    public List<Task> markColumnAsDone(@NonNull Long columnToMark, Long columnToUnmark) {
        Stream<Task> completedTasks = columnRepo.findById(columnToMark)
                .map(column -> column.setDoneColumn(true))
                .map(ProjectColumn::getTasks)
                .stream()
                .flatMap(Collection::stream)
                .map(task -> task.setCompleted(true));

        Stream<Task> uncompletedTasks = Optional.ofNullable(columnToUnmark)
                .flatMap(columnRepo::findById)
                .map(column -> column.setDoneColumn(false))
                .map(ProjectColumn::getTasks)
                .stream()
                .flatMap(Collection::stream)
                .map(task -> task.setCompleted(false));

        return Stream.concat(completedTasks, uncompletedTasks)
                .toList();
    }

    private ProjectColumn deleteAndShift(ProjectColumn columnToDelete, List<ProjectColumn> columns) {
        columnRepo.delete(columnToDelete);

        columns.stream()
                .filter(column -> column.getIndex() > columnToDelete.getIndex())
                .forEach(column -> column.setIndex(column.getIndex() - 1));

        return columnToDelete;
    }

    private static ClientSideException buildException(Long columnId) {
        return new ClientSideException(NOT_FOUND_MESSAGE.formatted(columnId), HttpStatus.NOT_FOUND);
    }
}
