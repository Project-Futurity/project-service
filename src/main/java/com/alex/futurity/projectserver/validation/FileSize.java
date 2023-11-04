package com.alex.futurity.projectserver.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileSizeValidator.class)
public @interface FileSize {
    String message() default "com.alex.futurity.userserver.validation.FileSize";
    int max() default 1024;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
