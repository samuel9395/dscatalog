package com.project.dscatalog.services.validation;

import jakarta.validation.Constraint;      // ← mudar para jakarta
import jakarta.validation.Payload;         // ← mudar para jakarta

import java.lang.annotation.*;

@Constraint(validatedBy = UserInsertValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)

public @interface UserInsertValid {
    String message() default "Validation error";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}