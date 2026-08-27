package io.github.carlosmanoelwendorff1.smartScheduller.resource.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.carlosmanoelwendorff1.smartScheduller.resource.controllers.ResourceController;
import io.github.carlosmanoelwendorff1.smartScheduller.resource.domain.exception.ResourceNotFoundException;

@RestControllerAdvice(basePackageClasses = ResourceController.class)
public class ResourceExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidInput(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}