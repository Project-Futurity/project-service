package com.alex.futurity.projectserver.validation;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FileSizeValidator implements ConstraintValidator<FileSize, MultipartFile> {
    private int maxSize;

    @Override
    public void initialize(FileSize file) {
        this.maxSize = file.max();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext constraintValidatorContext) {
        long fileSize = file.getSize();

        return maxSize > fileSize;
    }
}
