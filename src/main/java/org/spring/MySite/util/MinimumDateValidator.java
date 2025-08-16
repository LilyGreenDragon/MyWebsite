package org.spring.MySite.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MinimumDateValidator implements ConstraintValidator<MinimumDate, LocalDate> {
    private LocalDate minimumDate;
    //private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public void initialize(MinimumDate constraintAnnotation) {
        minimumDate = LocalDate.parse(constraintAnnotation.value());
        //minimumDate = LocalDate.parse(constraintAnnotation.value(),  DATE_FORMATTER);
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        return value == null || !value.isBefore(minimumDate);
    }
}