package com.alex.futurity.projectserver.validation;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class FileTypeValidator implements ConstraintValidator<FileType, MultipartFile> {
    private List<String> types;

    @Override
    public void initialize(FileType fileType) {
        this.types = List.of(fileType.types());
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext constraintValidatorContext) {
        String filename = file.getOriginalFilename();
        int i = filename.lastIndexOf('.');

        if (i == -1) {
            return false;
        }

        String extension = filename.substring(i + 1);

        return types.contains(extension);
    }
}
