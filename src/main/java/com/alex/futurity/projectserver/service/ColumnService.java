package com.alex.futurity.projectserver.service;

import com.alex.futurity.projectserver.dto.CreationTaskDto;
import com.alex.futurity.projectserver.dto.ProjectColumnDto;
import com.alex.futurity.projectserver.entity.Project;
import com.alex.futurity.projectserver.entity.ProjectColumn;
import com.alex.futurity.projectserver.entity.Task;
import com.alex.futurity.projectserver.exception.ClientSideException;
import com.alex.futurity.projectserver.repo.ColumnRepository;
import com.alex.futurity.projectserver.service.ProjectService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ColumnService {
    private ProjectService projectService;
    private ColumnRepository columnRepo;
    private static final String NOT_FOUND_MESSAGE = "The column is associated with such data does not exist";

    public long createColumn(long userId, long projectId, String columnName) {
        ProjectColumn projectColumn = projectService.addColumnToProject(userId, projectId, columnName);

        return projectColumn.getId();
    }

    @Transactional
    public List<ProjectColumnDto> getColumns(long userId, long projectId) {
        List<ProjectColumn> columns = findProjectColumnsForProject(projectId, userId, false);

        return columns.stream()
                .map(ProjectColumnDto::fromProjectColumn)
                .collect(Collectors.toList());
    }


    @Transactional
    public void deleteColumn(long userId, long projectId, long columnId) {
        List<ProjectColumn> columns = findProjectColumnsForProject(projectId, userId, true);
        ProjectColumn columnToDelete = columns.stream()
                .filter(column -> column.getId() == columnId)
                .findFirst()
                .orElseThrow(() -> new ClientSideException(NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));

        columns.stream()
                .filter(column -> column.getIndex() > columnToDelete.getIndex())
                .forEach(column -> column.setIndex(column.getIndex() - 1));

        columnRepo.delete(columnToDelete);
    }


    @Transactional
    public void changeColumnIndex(long userId, long projectId, int from, int to) {
        List<ProjectColumn> columns = findProjectColumnsForProject(projectId, userId, true);

        if (from < 0 || from > columns.size() || to < 0 || to > columns.size() + 1 || from == to) {
            throw new ClientSideException("Columns out of bounds", HttpStatus.BAD_REQUEST);
        }

        ProjectColumn columnFrom = columns.get(from);
        columnFrom.setIndex(to);

        if (from < to) {
            columns.stream()
                    .filter(column -> column.getIndex() >= from && column.getIndex() <= to
                            && !Objects.equals(columnFrom.getId(), column.getId()))
                    .forEach(column -> column.setIndex(column.getIndex() - 1));
        } else {
            columns.stream()
                    .filter(column -> column.getIndex() >= to && column.getIndex() <= from
                            && !Objects.equals(columnFrom.getId(), column.getId()))
                    .forEach(column -> column.setIndex(column.getIndex() + 1));
        }
    }

    @Transactional
    public Task addTaskToColumn(long userId, long projectId, long columnId, CreationTaskDto creationTaskDto) {
        ProjectColumn column = findColumnByColumnId(projectId, userId, columnId);
        Task task = new Task(creationTaskDto.getName(), creationTaskDto.getDeadline(), column);
        column.addTask(task);
        return task;
    }

    @Transactional
    public void changeColumnName(long userId, long projectId, long columnId, String columnName) {
        ProjectColumn column = findColumnByColumnId(projectId, userId, columnId);
        column.setName(columnName);
    }

    public List<Task> getTasksFromColumn(long userId, long projectId, long columnId) {
        ProjectColumn projectColumn = findColumnByColumnId(projectId, userId, columnId);

        return projectColumn.getTasks();
    }

    @Transactional
    public void markColumnAsDone(long userId, long projectId, long columnToMark, Long columnToUnmark) {
        ProjectColumn column = findColumnByColumnId(projectId, userId, columnToMark);
        column.setDoneColumn(true);

        Optional.ofNullable(columnToUnmark).ifPresent(id -> {
            ProjectColumn colToUnmark = findColumnByColumnId(projectId, userId, id);
            colToUnmark.setDoneColumn(false);
        });
    }

    private List<ProjectColumn> findProjectColumnsForProject(long projectId, long userId, boolean checkSize) {
        List<ProjectColumn> columns = columnRepo.findAllByProjectIdAndProjectUserIdOrderByIndex(projectId, userId);

        if (columns.isEmpty() && checkSize) {
            throw new ClientSideException(NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND);
        }

        return columns;
    }

    private ProjectColumn findColumnByColumnId(long projectId, long userId, long columnId) {
        ProjectColumn projectColumn = columnRepo.findById(columnId)
                .orElseThrow(() -> new ClientSideException(NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));
        Project project = projectColumn.getProject();

        if (project.getId() != projectId || project.getUserId() != userId) {
            throw new ClientSideException(NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND);
        }

        return projectColumn;
    }
}
