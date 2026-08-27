package io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.exception;

public class DuplicateUserEmailException extends RuntimeException {
    public DuplicateUserEmailException(String email) {
        super("A user with email '" + email + "' already exists.");
    }
}