package com.alex.futurity.projectserver.controller;

import com.alex.futurity.projectserver.context.UserContext;
import com.alex.futurity.projectserver.dto.CreationProjectRequestDto;
import com.alex.futurity.projectserver.dto.ProjectDto;
import com.alex.futurity.projectserver.dto.RequestStringDto;
import com.alex.futurity.projectserver.model.UserProject;
import com.alex.futurity.projectserver.service.ProjectService;
import com.alex.futurity.projectserver.validation.FileNotEmpty;
import com.alex.futurity.projectserver.validation.FileSize;
import com.alex.futurity.projectserver.validation.FileType;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@AllArgsConstructor
@Slf4j
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public long createProject(@RequestPart @FileNotEmpty(message = "Preview must not be empty")
    @FileSize(max = 5 * (1024 * 1024),
            message = "Preview is too large. Max size 5MB")
    @FileType(types = {"jpeg", "jpg", "png", "gif"},
            message = "Wrong image type. " +
                    "Must be one of the following: .jpeg, .png, .gif") MultipartFile preview, @Valid @RequestPart CreationProjectRequestDto project) {
        project.setUserId(UserContext.getUserId());
        project.setPreview(preview);

        log.info("Handling creation project request. User id: {}, project: {}", UserContext.getUserId(), project);

        return projectService.createProject(project);
    }

    @GetMapping("/projects")
    public List<ProjectDto> getProjects() {
        log.info("Handling getting projects request. User id: {}", UserContext.getUserId());

        return projectService.getProjects(UserContext.getUserId());
    }

    @GetMapping(value = "/preview/{previewId}", produces = {
            MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE
    })
    public ResponseEntity<Resource> getPreview(@PathVariable(name = "previewId") long projectId) {
        log.info("Handling getting project preview. User id: {}, project id: {}", UserContext.getUserId(), projectId);

        return ResponseEntity.ok(projectService.findProjectPreview(UserProject.of(UserContext.getUserId(), projectId)));
    }

    @DeleteMapping("/delete/{projectId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteProject(@PathVariable long projectId) {
        log.info("Handling deleting project. User id: {}, project id: {}", UserContext.getUserId(), projectId);

        projectService.deleteProject(UserProject.of(UserContext.getUserId(), projectId));
    }

    @PatchMapping("/{projectId}/name")
    @ResponseStatus(HttpStatus.OK)
    public void changeProjectName(@PathVariable long projectId, @Valid @RequestBody RequestStringDto projectName) {
        log.info("Handling changing project name. User id: {}, project id: {}, project name: {}",
                UserContext.getUserId(), projectId, projectName.getValue());

        projectService.changeProjectName(UserProject.of(UserContext.getUserId(), projectId), projectName.getValue());
    }

    @PatchMapping("/{projectId}/description")
    @ResponseStatus(HttpStatus.OK)
    public void changeProjectDescription(@PathVariable long projectId,
                                         @Valid @RequestBody RequestStringDto projectDescription) {
        log.info("Handling changing project description. User id: {}, project id: {}, project description: {}",
                UserContext.getUserId(), projectId, projectDescription.getValue());

        projectService.changeProjectDescription(UserProject.of(UserContext.getUserId(), projectId), projectDescription.getValue());
    }
}
