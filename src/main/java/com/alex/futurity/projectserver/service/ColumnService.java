package com.alex.futurity.projectserver.service;

import com.alex.futurity.projectserver.dto.CreationTaskDto;
import com.alex.futurity.projectserver.dto.ProjectColumnDto;
import com.alex.futurity.projectserver.entity.ProjectColumn;
import com.alex.futurity.projectserver.entity.Task;
import com.alex.futurity.projectserver.exception.ClientSideException;
import com.alex.futurity.projectserver.model.UserProject;
import com.alex.futurity.projectserver.repo.ColumnRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ColumnService {
    private final ProjectService projectService;
    private final ColumnRepository columnRepo;
    private static final String NOT_FOUND_MESSAGE = "The column is associated with such data does not exist: %s";

    public long createColumn(@NonNull UserProject project, @NonNull String columnName) {
        return projectService.addColumnToProject(project, columnName).getId();
    }

    @Transactional
    public List<ProjectColumnDto> getColumns(@NonNull UserProject project) {
        return getColumnsByProject(project)
                .stream()
                .map(ProjectColumnDto::fromProjectColumn)
                .toList();
    }

    @Transactional
    public void deleteColumn(@NonNull UserProject project, @NonNull Long columnId) {
        List<ProjectColumn> columns = getColumnsByProject(project);

        columns.stream()
                .filter(column -> Objects.equals(column.getId(), columnId))
                .findFirst()
                .ifPresent(column -> deleteAndShift(column, columns));
    }


    @Transactional
    public void changeColumnIndex(@NonNull UserProject project, @NonNull Integer from, @NonNull Integer to) {
        List<ProjectColumn> columns = getColumnsByProject(project);

        if (from > columns.size() || to > columns.size() + 1) {
            throw new ClientSideException("Columns out of bounds", HttpStatus.BAD_REQUEST);
        }

        ProjectColumn columnFrom = columns.get(from);
        columnFrom.setIndex(to);

        if (from < to) {
            columns.stream()
                    .filter(column -> !Objects.equals(columnFrom.getId(), column.getId()))
                    .filter(column -> column.getIndex() >= from && column.getIndex() <= to)
                    .forEach(column -> column.setIndex(column.getIndex() - 1));
        } else {
            columns.stream()
                    .filter(column -> !Objects.equals(columnFrom.getId(), column.getId()))
                    .filter(column -> column.getIndex() >= to && column.getIndex() <= from)
                    .forEach(column -> column.setIndex(column.getIndex() + 1));
        }
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

    public ProjectColumn getColumnByIndex(@NonNull UserProject project, @NonNull Integer columnIndex) {
        return getColumnsByProject(project)
                .stream()
                .filter(projectColumn -> Objects.equals(columnIndex, projectColumn.getIndex()))
                .findFirst()
                .orElseThrow(() -> new ClientSideException("Not Found by user project: " + project, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void markColumnAsDone(@NonNull Long columnToMark, Long columnToUnmark) {
        columnRepo.findById(columnToMark)
                        .ifPresent(column -> column.setDoneColumn(true));

        Optional.ofNullable(columnToUnmark)
                .flatMap(columnRepo::findById)
                .ifPresent(column -> column.setDoneColumn(false));
    }

    private List<ProjectColumn> getColumnsByProject(UserProject project) {
        return columnRepo.findAllByProject(project.getUserId(), project.getProjectId());
    }

    private void deleteAndShift(ProjectColumn columnToDelete, List<ProjectColumn> columns) {
        columnRepo.delete(columnToDelete);

        columns.stream()
                .filter(column -> column.getIndex() > columnToDelete.getIndex())
                .forEach(column -> column.setIndex(column.getIndex() - 1));
    }

    private ClientSideException buildException(Long columnId) {
        return new ClientSideException(NOT_FOUND_MESSAGE.formatted(columnId), HttpStatus.NOT_FOUND);
    }
}
