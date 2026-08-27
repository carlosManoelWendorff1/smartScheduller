package io.github.carlosmanoelwendorff1.smartScheduller.identity.services;

import org.springframework.stereotype.Service;

import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.model.LoginResult;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.model.User;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.security.JwtService;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthService(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    public LoginResult login(String email, String password) {
        User user = userService.authenticate(email, password);
        String token = jwtService.generateToken(user.getId(), user.getTenantId(), user.getRole().name());
        return new LoginResult(token, user.getId(), user.getTenantId(), user.getRole().name());
    }
}