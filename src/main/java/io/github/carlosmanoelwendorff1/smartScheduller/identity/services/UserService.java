package io.github.carlosmanoelwendorff1.smartScheduller.identity.services;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.exception.DuplicateUserEmailException;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.exception.InvalidCredentialsException;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.model.Role;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.model.User;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(UUID tenantId, String name, String email, String rawPassword, Role role) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateUserEmailException(email);
        }

        String passwordHash = passwordEncoder.encode(rawPassword);
        User user = User.create(tenantId, name, email, passwordHash, role);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive() || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }
}