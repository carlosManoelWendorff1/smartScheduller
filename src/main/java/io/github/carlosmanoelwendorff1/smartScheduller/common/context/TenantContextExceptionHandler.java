package io.github.carlosmanoelwendorff1.smartScheduller.common.context;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global (not scoped to a single controller package) because every module
 * that adopts TenantContext should get this behavior automatically.
 */
@RestControllerAdvice
public class TenantContextExceptionHandler {

    @ExceptionHandler(MissingTenantHeaderException.class)
    public ProblemDetail handleMissingTenant(MissingTenantHeaderException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}