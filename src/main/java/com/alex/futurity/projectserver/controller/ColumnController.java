package com.alex.futurity.projectserver.controller;

import com.alex.futurity.projectserver.context.UserContext;
import com.alex.futurity.projectserver.dto.ProjectColumnDto;
import com.alex.futurity.projectserver.dto.RequestStringDto;
import com.alex.futurity.projectserver.model.UserProject;
import com.alex.futurity.projectserver.service.ColumnService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/column/{projectId}")
public class ColumnController {
    private final ColumnService columnService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public long createColumn(@PathVariable long projectId,
                             @Valid @RequestBody RequestStringDto columnName) {
        log.info("Handling creation column request. User id: {}, project id: {}, name: {}",
                UserContext.getUserId(), projectId, columnName.getValue());

        return columnService.createColumn(UserProject.of(UserContext.getUserId(), projectId), columnName.getValue());
    }

    @GetMapping
    public List<ProjectColumnDto> getColumns(@PathVariable long projectId) {
        log.info("Handling get columns request. User id: {}, project id: {}",
                UserContext.getUserId(), projectId);

        return columnService.getColumns(UserProject.of(UserContext.getUserId(), projectId));
    }

    @DeleteMapping("/{columnId}/delete")
    @ResponseStatus(HttpStatus.OK)
    public void deleteColumn(@PathVariable long projectId, @PathVariable long columnId) {
        log.info("Handling deleting column request. User id: {}, project id: {}, column id: {}",
                UserContext.getUserId(), projectId, columnId);

        columnService.deleteColumn(UserProject.of(UserContext.getUserId(), projectId), columnId);
    }

    @PatchMapping("/index/change")
    @ResponseStatus(HttpStatus.OK)
    public void changeIndexColumn(@PathVariable long projectId,
                                  @RequestParam int from, @RequestParam int to) {
        log.info("Handling changing column index request. User id: {}, project id: {}, from {} to {}",
                UserContext.getUserId(), projectId, from, to);

        columnService.changeColumnIndex(UserProject.of(UserContext.getUserId(), projectId), from, to);
    }

    @PatchMapping("/{columnId}/name")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void changeColumnName(@PathVariable long projectId, @PathVariable long columnId,
                                 @Valid @RequestBody RequestStringDto columnName) {
        log.info("Handling changing column name request. User id: {}, project id: {}, column id: {}, columnName: {}",
                UserContext.getUserId(), projectId, columnId, columnName.getValue());

        columnService.changeColumnName(columnId, columnName.getValue());
    }

    @PatchMapping("/mark")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void markColumnAsDone(@PathVariable long projectId,
                                 @RequestParam(value = "columnIdToUnmark", required = false) Long columnToUnmark,
                                 @RequestParam(value = "columnIdToMark") long columnToMark) {
        log.info("Handlong marking column request. User id: {}, project id: {}, column to mark: {}, column to unmark: {}",
                UserContext.getUserId(), projectId, columnToMark, columnToUnmark);

        columnService.markColumnAsDone(columnToMark, columnToUnmark);
    }
}
