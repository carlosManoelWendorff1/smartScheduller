package io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.exception.TenantTimezoneUnavailableException;

@RestControllerAdvice(basePackageClasses = {
        BusinessHoursController.class, ProfessionalAvailabilityRuleController.class, AvailabilityController.class
})
public class AvailabilityExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidInput(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(TenantTimezoneUnavailableException.class)
    public ProblemDetail handleTimezoneUnavailable(TenantTimezoneUnavailableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Invalid professionalId (doesn't exist or doesn't belong to your tenant).");
    }
}