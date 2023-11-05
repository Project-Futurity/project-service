package com.alex.futurity.projectserver.dto;

import com.alex.futurity.projectserver.entity.Project;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import java.io.IOException;

@Getter
@Setter
@AllArgsConstructor
@ToString(exclude = "preview")
public class CreationProjectRequestDto {
    @NotBlank(message = "Wrong name. Name must not be empty")
    private String name;
    @NotBlank(message = "Wrong description. Description must not be empty")
    private String description;

    @Null
    private MultipartFile preview;
    @Null
    private Long userId;

    public Project toProject() {
        return new Project(userId, name, description, readPreview());
    }

    private byte[] readPreview() {
        try {
            return preview.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("The preview cannot be read", e);
        }
    }
}
