package io.github.carlosmanoelwendorff1.smartScheduller.identity.security;

public class InvalidJwtException extends RuntimeException {
    public InvalidJwtException(String message) {
        super(message);
    }
}