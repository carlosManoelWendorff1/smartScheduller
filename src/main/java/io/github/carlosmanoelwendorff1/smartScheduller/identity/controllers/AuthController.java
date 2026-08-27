// identity/controllers/AuthController.java
package io.github.carlosmanoelwendorff1.smartScheduller.identity.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.carlosmanoelwendorff1.smartScheduller.identity.controllers.dto.LoginRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.controllers.dto.LoginResponse;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.services.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return LoginResponse.from(authService.login(request.email(), request.password()));
    }
}