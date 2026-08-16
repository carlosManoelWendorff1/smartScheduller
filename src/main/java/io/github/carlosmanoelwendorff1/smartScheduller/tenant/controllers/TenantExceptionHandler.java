package io.github.carlosmanoelwendorff1.smartScheduller.tenant.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.exception.TenantNotFoundException;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.exception.TenantSlugAlreadyInUseException;

@RestControllerAdvice(basePackageClasses = TenantController.class)
public class TenantExceptionHandler {

    @ExceptionHandler(TenantNotFoundException.class)
    public ProblemDetail handleNotFound(TenantNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TenantSlugAlreadyInUseException.class)
    public ProblemDetail handleSlugConflict(TenantSlugAlreadyInUseException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidInput(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleInvalidState(IllegalStateException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }
}
