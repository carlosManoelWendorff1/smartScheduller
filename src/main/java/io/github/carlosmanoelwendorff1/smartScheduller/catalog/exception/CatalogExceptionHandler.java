// catalog/controllers/CatalogExceptionHandler.java
package io.github.carlosmanoelwendorff1.smartScheduller.catalog.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.professional.ProfessionalController;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.service.ServiceController;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.exception.ProfessionalNotFoundException;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.exception.ServiceNotFoundException;

@RestControllerAdvice(basePackageClasses = { ServiceController.class, ProfessionalController.class })
public class CatalogExceptionHandler {

    @ExceptionHandler(ServiceNotFoundException.class)
    public ProblemDetail handleServiceNotFound(ServiceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ProfessionalNotFoundException.class)
    public ProblemDetail handleProfessionalNotFound(ProfessionalNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidInput(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}