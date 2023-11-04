package com.alex.futurity.projectserver.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileTypeValidator.class)
public @interface FileType {
    String message() default "com.alex.futurity.userserver.validation.FileType";
    String[] types() default {};

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
