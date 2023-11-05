package com.alex.futurity.projectserver.controller;

import com.alex.futurity.projectserver.dto.*;
import com.alex.futurity.projectserver.model.UserProject;
import com.alex.futurity.projectserver.service.ProjectService;
import com.alex.futurity.projectserver.validation.FileNotEmpty;
import com.alex.futurity.projectserver.validation.FileSize;
import com.alex.futurity.projectserver.validation.FileType;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

@Validated
@RestController
@AllArgsConstructor
@Log4j2
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping("/{userId}/create")
    @ResponseStatus(HttpStatus.CREATED)
    public long createProject(@PathVariable long userId, @RequestPart @FileNotEmpty(message = "Preview must not be empty")
    @FileSize(max = 5 * (1024 * 1024),
            message = "Preview is too large. Max size 5MB")
    @FileType(types = {"jpeg", "jpg", "png", "gif"},
            message = "Wrong image type. " +
                    "Must be one of the following: .jpeg, .png, .gif") MultipartFile preview, @Valid @RequestPart CreationProjectRequestDto project) {
        project.setUserId(userId);
        project.setPreview(preview);

        log.info("Handling creation project request. User id: {}, project: {}", userId, project);

        return projectService.createProject(project);
    }

    @GetMapping("/{userId}/projects")
    public List<ProjectDto> getProjects(@PathVariable long userId) {
        log.info("Handling getting projects request. User id: {}", userId);

        return projectService.getProjects(userId);
    }

    @GetMapping(value = "/{userId}/preview/{previewId}", produces = {
            MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE
    })
    public ResponseEntity<Resource> getPreview(@PathVariable long userId, @PathVariable(name = "previewId") long projectId) {
        log.info("Handling getting project preview. User id: {}, project id: {}", userId, projectId);

        return ResponseEntity.ok(projectService.findProjectPreview(UserProject.of(userId, projectId)));
    }

    @DeleteMapping("/{userId}/delete/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteProject(@PathVariable long userId, @PathVariable long projectId) {
        log.info("Handling deleting project. User id: {}, project id: {}", userId, projectId);

        projectService.deleteProject(UserProject.of(userId, projectId));
    }

    @PatchMapping("/{userId}/{projectId}/name")
    @ResponseStatus(HttpStatus.OK)
    public void changeProjectName(@PathVariable long userId, @PathVariable long projectId, @Valid @RequestBody RequestStringDto projectName) {
        log.info("Handling changing project name. User id: {}, project id: {}, project name: {}", userId, projectId, projectName.getValue());

        projectService.changeProjectName(UserProject.of(userId, projectId), projectName.getValue());
    }

    @PatchMapping("/{userId}/{projectId}/description")
    @ResponseStatus(HttpStatus.OK)
    public void changeProjectDescription(@PathVariable long userId, @PathVariable long projectId,
                                         @Valid @RequestBody RequestStringDto projectDescription) {
        log.info("Handling changing project description. User id: {}, project id: {}, project description: {}",
                userId, projectId, projectDescription.getValue());

        projectService.changeProjectDescription(UserProject.of(userId, projectId), projectDescription.getValue());
    }
}
