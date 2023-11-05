package com.alex.futurity.projectserver.controller;

import com.alex.futurity.projectserver.dto.CreationProjectRequestDto;
import com.alex.futurity.projectserver.dto.ProjectDto;
import com.alex.futurity.projectserver.entity.Project;
import com.alex.futurity.projectserver.exception.ErrorMessage;
import com.alex.futurity.projectserver.repo.ProjectRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProjectControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProjectRepository projectRepository;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Container
    private final static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:latest")
                    .withUsername("postgres")
                    .withPassword("root");

    private final static String VALID_NAME = "Spring learning";
    private final static String VALID_DESCRIPTION = "Learning Spring boot";
    private final static MockMultipartFile VALID_PREVIEW =
            new MockMultipartFile("preview", "project-preview.jpeg", MediaType.IMAGE_JPEG_VALUE, new byte[1]);

    @Test
    @SneakyThrows
    @DisplayName("Should create project if a project request is valid")
    void testCreateProject() {
        Long id = 1L;
        CreationProjectRequestDto dto = new CreationProjectRequestDto(VALID_NAME, VALID_DESCRIPTION, null, null);
        String content = mockMvc.perform(multipart("/" + id + "/create")
                        .file(VALID_PREVIEW)
                        .part(buildMockPart(dto)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(getProject())
                .returns(Long.parseLong(content), Project::getId)
                .returns(VALID_NAME, Project::getName)
                .returns(id, Project::getUserId)
                .returns(VALID_DESCRIPTION, Project::getDescription)
                .returns(VALID_PREVIEW.getBytes(), Project::getPreview);
    }

    @ParameterizedTest
    @MethodSource("getInvalidProjectRequests")
    @SneakyThrows
    @DisplayName("Should return error if a project request is invalid")
    void testCreateProjectWithInvalidRequest(CreationProjectRequestDto dto, MockMultipartFile preview, ErrorMessage message) {
        mockMvc.perform(multipart("/1/create")
                        .file(preview)
                        .part(buildMockPart(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(message)));
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return error if the project with the same name exist")
    void testCreateProjectWithExistingName() {
        long id = 1L;
        Project project = new Project(id, VALID_NAME, VALID_DESCRIPTION, VALID_PREVIEW.getBytes());
        createProject(project);
        CreationProjectRequestDto dto = new CreationProjectRequestDto(VALID_NAME, VALID_DESCRIPTION, null, null);

        mockMvc.perform(multipart("/" + id + "/create")
                        .file(VALID_PREVIEW)
                        .part(buildMockPart(dto)))
                .andExpect(status().isConflict())
                .andExpect(content().json(objectMapper.writeValueAsString(new ErrorMessage("Project with such name exists"))));
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return projects by id")
    void testGetProjectsByUserId() {
        long id = 1L;
        Project project = new Project(id, VALID_NAME, VALID_DESCRIPTION, VALID_PREVIEW.getBytes());
        createProject(project);
        long projectId = getProject().getId();

        String result = mockMvc.perform(get("/" + id + "/projects"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<ProjectDto> projects = objectMapper.readValue(result, new TypeReference<List<ProjectDto>>() {});
        assertThat(projects)
                .singleElement()
                .returns(id, ProjectDto::getId)
                .returns(VALID_DESCRIPTION, ProjectDto::getDescription)
                .returns(VALID_NAME, ProjectDto::getName)
                .returns("/preview/" + projectId, ProjectDto::getPreviewUrl);
    }

    @Test
    @SneakyThrows
    @DisplayName("Should load the preview")
    void testGetPreview() {
        long id = 1L;
        Project project = new Project(id, VALID_NAME, VALID_DESCRIPTION, VALID_PREVIEW.getBytes());
        createProject(project);
        long projectId = getProject().getId();

        mockMvc.perform(get("/" + id + "/preview/" + projectId))
                .andExpect(status().isOk())
                .andExpect(content().bytes(VALID_PREVIEW.getBytes()));
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return error if review is not found")
    void testGetNotExistingPreview() {
        mockMvc.perform(get("/1/preview/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(
                        objectMapper.writeValueAsString(new ErrorMessage("The project is associated with such data does not exist: 1 1"))
                ));
    }

    @Test
    @SneakyThrows
    @DisplayName("Should delete project if it exists")
    void testDeleteProject() {
        long id = 1L;
        Project project = new Project(id, VALID_NAME, VALID_DESCRIPTION, VALID_NAME.getBytes());
        createProject(project);
        long projectId = getProject().getId();

        mockMvc.perform(delete("/" + id + "/delete/" + projectId))
                .andExpect(status().isOk());

        List<Project> projects = projectRepository.findAll();
        assertThat(projects).isEmpty();
    }

    @Test
    @SneakyThrows
    @DisplayName("Should return an error if a project is not found")
    void testDeleteNotExistingProject() {
        mockMvc.perform(delete("/1/delete/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(new ErrorMessage("The project is associated with such data does not exist: 1 1"))
                ));
    }

    private static Stream<Arguments> getInvalidProjectRequests() {
        MockMultipartFile fileEmpty = new MockMultipartFile("preview", "preview.jpeg", MediaType.IMAGE_JPEG_VALUE,
                new byte[0]);
        MockMultipartFile largeFile = new MockMultipartFile("preview", "preview.jpeg", MediaType.IMAGE_JPEG_VALUE,
                new byte[6 * (1024 * 1024)]);
        MockMultipartFile wrongType = new MockMultipartFile("preview", "preview.docx", MediaType.IMAGE_JPEG_VALUE,
                new byte[10]);
        MockMultipartFile withoutType = new MockMultipartFile("preview", "preview", MediaType.IMAGE_JPEG_VALUE,
                new byte[10]);

        CreationProjectRequestDto validDto = new CreationProjectRequestDto(VALID_NAME, VALID_DESCRIPTION,
                null, null);

        // invalid names
        CreationProjectRequestDto dtoWithNullName = new CreationProjectRequestDto(null, VALID_DESCRIPTION, null, null);
        CreationProjectRequestDto dtoWithEmptyName = new CreationProjectRequestDto("", VALID_DESCRIPTION, null, null);
        CreationProjectRequestDto dtoWithBlankName = new CreationProjectRequestDto("      ", VALID_DESCRIPTION, null, null);

        // invalid descriptions
        CreationProjectRequestDto dtoWithNullDescription = new CreationProjectRequestDto(VALID_NAME, null, null, null);
        CreationProjectRequestDto dtoWithEmptyDescription = new CreationProjectRequestDto(VALID_NAME, "", null, null);
        CreationProjectRequestDto dtoWithBlankDescription = new CreationProjectRequestDto(VALID_NAME, "      ", null, null);

        return Stream.of(
                // invalid files
                Arguments.of(validDto, fileEmpty, new ErrorMessage("Preview must not be empty")),
                Arguments.of(validDto, largeFile, new ErrorMessage("Preview is too large. Max size 5MB")),
                Arguments.of(validDto, wrongType, new ErrorMessage("Wrong image type. Must be one of the following: .jpeg, .png, .gif")),
                Arguments.of(validDto, withoutType, new ErrorMessage("Wrong image type. Must be one of the following: .jpeg, .png, .gif")),

                // invalid names
                Arguments.of(dtoWithNullName, VALID_PREVIEW, new ErrorMessage("Wrong name. Name must not be empty")),
                Arguments.of(dtoWithEmptyName, VALID_PREVIEW, new ErrorMessage("Wrong name. Name must not be empty")),
                Arguments.of(dtoWithBlankName, VALID_PREVIEW, new ErrorMessage("Wrong name. Name must not be empty")),

                // invalid descriptions
                Arguments.of(dtoWithNullDescription, VALID_PREVIEW, new ErrorMessage("Wrong description. Description must not be empty")),
                Arguments.of(dtoWithEmptyDescription, VALID_PREVIEW, new ErrorMessage("Wrong description. Description must not be empty")),
                Arguments.of(dtoWithBlankDescription, VALID_PREVIEW, new ErrorMessage("Wrong description. Description must not be empty"))
        );
    }

    private void createProject(Project project) {
        projectRepository.save(project);
    }

    private Project getProject() {
        List<Project> projects = projectRepository.findAll();
        assertThat(projects).hasSize(1);

        return projects.get(0);
    }

    @DynamicPropertySource
    public static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
    }

    @SneakyThrows
    private <T> MockPart buildMockPart(T dto) {
        MockPart project = new MockPart("project", objectMapper.writeValueAsBytes(dto));
        project.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        return project;
    }
}