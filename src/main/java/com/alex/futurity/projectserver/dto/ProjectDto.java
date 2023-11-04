package com.alex.futurity.projectserver.dto;

import com.alex.futurity.projectserver.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDto {
    private long id;
    private String name;
    private String description;
    private String previewUrl;

    public static ProjectDto fromProject(Project project) {
        String previewUrl = "/preview/" + project.getId();

        return new ProjectDto(
                project.getId(), project.getName(), project.getDescription(), previewUrl
        );
    }
}
