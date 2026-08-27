package io.github.carlosmanoelwendorff1.smartScheduller.identity.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.exception.DuplicateUserEmailException;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.exception.InvalidCredentialsException;

@RestControllerAdvice(basePackageClasses = { UserController.class, AuthController.class })
public class IdentityExceptionHandler {

    @ExceptionHandler(DuplicateUserEmailException.class)
    public ProblemDetail handleDuplicateEmail(DuplicateUserEmailException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidInput(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}