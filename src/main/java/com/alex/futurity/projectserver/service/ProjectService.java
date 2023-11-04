package com.alex.futurity.projectserver.service;

import com.alex.futurity.projectserver.dto.*;
import com.alex.futurity.projectserver.entity.Project;
import com.alex.futurity.projectserver.entity.ProjectColumn;
import com.alex.futurity.projectserver.exception.ClientSideException;
import com.alex.futurity.projectserver.repo.ProjectRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Log4j2
public class ProjectService {
    private final ProjectRepository projectRepo;
    private static final String NOT_FOUND_MESSAGE = "The project is associated with such data does not exist";

    @Transactional
    public long createProject(CreationProjectRequestDto dto) {
        try {
            if (hasUserProjectWithName(dto.getName(), dto.getUserId())) {
                throw new ClientSideException("Project with such name exists", HttpStatus.CONFLICT);
            }

            Project project = dto.toProject();
            projectRepo.save(project);
            log.info("The project with name {} has been saved for user with {} id", project.getName(), project.getUserId());
            return project.getId();
        } catch (IOException e) {
            log.warn("The preview {} cannot be read: {}", dto.getPreview(), e.getMessage());
            throw new IllegalStateException("The preview cannot be read");
        }
    }

    @Transactional
    public List<ProjectDto> getProjects(long id) {
        List<Project> projects = projectRepo.findAllByUserId(id);

        return projects.stream()
                .map(ProjectDto::fromProject)
                .sorted(Comparator.comparingLong(ProjectDto::getId))
                .collect(Collectors.toList());
    }


    @Transactional
    public Resource findProjectPreview(long userId, long projectId) {
        Project project = findByProjectIdAndUserId(projectId, userId);

        return new ByteArrayResource(project.getPreview());
    }


    @Transactional
    public void deleteProject(long userId, long projectId) {
        int deleted = projectRepo.deleteProjectByIdAndUserId(projectId, userId);

        if (deleted == 0) {
            throw new ClientSideException(NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public ProjectColumn addColumnToProject(long userId, long projectId, String columnName) {
        Project project = findByProjectIdAndUserId(projectId, userId);
        ProjectColumn projectColumn = new ProjectColumn(columnName, project);
        project.addColumn(projectColumn);

        return projectColumn;
    }

    @Transactional
    public void changeProjectName(long userId, long projectId, String projectName) {
        Project project = findByProjectIdAndUserId(projectId, userId);
        project.setName(projectName);
    }

    @Transactional
    public void changeProjectDescription(long userId, long projectId, String projectDescription) {
        Project project = findByProjectIdAndUserId(projectId, userId);
        project.setDescription(projectDescription);
    }

    private Project findByProjectIdAndUserId(long projectId, long userId) {
        return projectRepo.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ClientSideException(NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));
    }

    private boolean hasUserProjectWithName(String name, long userId) {
        return projectRepo.findByNameAndUserId(name, userId).isPresent();
    }
}
