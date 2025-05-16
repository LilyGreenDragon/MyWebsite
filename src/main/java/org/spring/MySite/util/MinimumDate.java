package org.spring.MySite.util;

import jakarta.validation.Constraint;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MinimumDateValidator.class)
@PastOrPresent
public @interface MinimumDate {
    String message() default "Date must not be before {value}";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};

    String value() default "1920-01-01";
}
