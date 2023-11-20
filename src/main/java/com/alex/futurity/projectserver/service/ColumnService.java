package com.alex.futurity.projectserver.service;

import com.alex.futurity.projectserver.dao.ColumnDao;
import com.alex.futurity.projectserver.dto.CreationTaskDto;
import com.alex.futurity.projectserver.dto.ProjectColumnDto;
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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class ColumnService {
    private final ProjectService projectService;
    private final ColumnDao columnDao;
    private final TaskEventPublisher eventPublisher;

    public long createColumn(@NonNull UserProject project, @NonNull String columnName) {
        return projectService.addColumnToProject(project, columnName).getId();
    }

    public List<ProjectColumnDto> getColumns(@NonNull UserProject project) {
        return columnDao.getColumns(project);
    }

    @Transactional
    public void deleteColumn(@NonNull UserProject project, @NonNull Long columnId) {
        columnDao.deleteColumn(project, columnId)
                .map(ProjectColumn::getTasks)
                .stream()
                .flatMap(Collection::stream)
                .filter(Task::hasDeadline)
                .forEach(eventPublisher::publishDeleteEvent);
    }

    @Transactional
    public void changeColumnIndex(@NonNull UserProject project, @NonNull Integer from, @NonNull Integer to) {
        List<ProjectColumn> columns = columnDao.getColumnsByProject(project)
                .stream()
                .sorted(Comparator.comparingInt(ProjectColumn::getIndex))
                .toList();

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


    public Task addTaskToColumn(@NonNull CreationTaskDto creationTaskDto, @NonNull Long columnId) {
        return columnDao.addTaskToColumn(creationTaskDto, columnId);
    }

    public void changeColumnName(@NonNull Long columnId, String columnName) {
        columnDao.changeColumnName(columnId, columnName);
    }

    public ProjectColumn getColumnByIndex(@NonNull UserProject project, @NonNull Integer columnIndex) {
        return columnDao.getColumnsByProject(project)
                .stream()
                .filter(projectColumn -> Objects.equals(columnIndex, projectColumn.getIndex()))
                .findFirst()
                .orElseThrow(() -> new ClientSideException("Not Found by user project: " + project, HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void markColumnAsDone(@NonNull Long columnToMark, Long columnToUnmark) {
        List<Task> tasks = columnDao.markColumnAsDone(columnToMark, columnToUnmark);

        tasks.stream()
                .filter(Task::hasDeadline)
                .forEach(eventPublisher::publishUpdateEvent);
    }
}
