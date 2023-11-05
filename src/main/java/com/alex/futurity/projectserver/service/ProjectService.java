package com.alex.futurity.projectserver.service;

import com.alex.futurity.projectserver.dto.*;
import com.alex.futurity.projectserver.entity.Project;
import com.alex.futurity.projectserver.entity.ProjectColumn;
import com.alex.futurity.projectserver.exception.ClientSideException;
import com.alex.futurity.projectserver.model.UserProject;
import com.alex.futurity.projectserver.repo.ColumnRepository;
import com.alex.futurity.projectserver.repo.ProjectRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Log4j2
public class ProjectService {
    private final ProjectRepository projectRepo;
    private final ColumnRepository columnRepo;
    private static final String NOT_FOUND_MESSAGE = "The project is associated with such data does not exist: %s %s";

    @Transactional
    public List<ProjectDto> getProjects(long id) {
        List<Project> projects = projectRepo.findAllByUserId(id);

        return projects.stream()
                .map(ProjectDto::fromProject)
                .sorted(Comparator.comparingLong(ProjectDto::getId))
                .toList();
    }

    @Transactional
    public long createProject(@NonNull CreationProjectRequestDto dto) {
        validateCreation(dto);
        Project project = save(dto.toProject());

        return project.getId();
    }


    @Transactional
    public Resource findProjectPreview(@NonNull UserProject userProject) {
        return getProjectByIdAndUserId(userProject)
                .map(project -> new ByteArrayResource(project.getPreview()))
                .orElseThrow(() -> buildException(userProject));
    }

    @Transactional
    public void deleteProject(@NonNull UserProject userProject) {
        int deleted = projectRepo.deleteProjectByIdAndUserId(userProject.getProjectId(), userProject.getUserId());

        if (deleted == 0) {
            throw buildException(userProject);
        }
    }

    @Transactional
    public ProjectColumn addColumnToProject(@NonNull UserProject userProject, @NonNull String columnName) {
        return getProjectByIdAndUserId(userProject)
                .map(project -> project.addColumn(new ProjectColumn(columnName, project)))
                .orElseThrow(() -> buildException(userProject));
    }

    @Transactional
    public void changeProjectName(@NonNull UserProject userProject, @NonNull String projectName) {
        getProjectByIdAndUserId(userProject)
                .ifPresent(project -> project.setName(projectName));
    }

    @Transactional
    public void changeProjectDescription(@NonNull UserProject userProject, @NonNull String projectDescription) {
        getProjectByIdAndUserId(userProject)
                .ifPresent(project -> project.setDescription(projectDescription));
    }

    private Optional<Project> getProjectByIdAndUserId(UserProject project) {
        return projectRepo.findByIdAndUserId(project.getProjectId(), project.getUserId());
    }

    private boolean hasUserProjectWithName(String name, long userId) {
        return projectRepo.findByNameAndUserId(name, userId).isPresent();
    }

    private Project save(Project projectToSave) {
        Project project = projectRepo.save(projectToSave);
        log.info("The project with name {} has been saved for user with {} id",
                project.getName(), project.getUserId());

        return project;
    }

    private void validateCreation(CreationProjectRequestDto dto) {
        if (hasUserProjectWithName(dto.getName(), dto.getUserId())) {
            throw new ClientSideException("Project with such name exists", HttpStatus.CONFLICT);
        }
    }

    private static ClientSideException buildException(UserProject userProject) {
        return new ClientSideException(
                NOT_FOUND_MESSAGE.formatted(userProject.getUserId(), userProject.getProjectId()),
                HttpStatus.NOT_FOUND
        );
    }
}
