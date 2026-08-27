package io.github.carlosmanoelwendorff1.smartScheduller.identity.controllers;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.carlosmanoelwendorff1.smartScheduller.AbstractIntegrationTest;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.controllers.dto.LoginRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.controllers.dto.RegisterUserRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.model.Role;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.security.JwtClaims;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.security.JwtService;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private JwtService jwtService;

    private UUID tenantId;

    @BeforeEach
    void setUp() throws Exception {
        Tenant tenant = tenantRepository.saveAndFlush(
                Tenant.create("Clinica Auth " + UUID.randomUUID(), "clinica-auth-" + UUID.randomUUID(),
                        "America/Sao_Paulo"));
        tenantId = tenant.getId();

        RegisterUserRequest registerRequest = new RegisterUserRequest("Maria Admin", "maria.login@example.com",
                "correct-password", Role.ADMIN);

        mockMvc.perform(post("/api/v1/tenants/{tenantId}/users", tenantId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void loginWithCorrectCredentialsReturnsAWorkingToken() throws Exception {
        LoginRequest request = new LoginRequest("maria.login@example.com", "correct-password");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tenantId", is(tenantId.toString())))
                .andExpect(jsonPath("$.role", is("ADMIN")))
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asString();

        // The token isn't just non-null - it actually decodes to the right tenant/role.
        JwtClaims claims = jwtService.parseAndValidate(token);
        assertThatTenantMatches(claims);
    }

    private void assertThatTenantMatches(JwtClaims claims) {
        org.assertj.core.api.Assertions.assertThat(claims.tenantId()).isEqualTo(tenantId);
        org.assertj.core.api.Assertions.assertThat(claims.role()).isEqualTo("ADMIN");
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        LoginRequest request = new LoginRequest("maria.login@example.com", "wrong-password");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithUnknownEmailReturns401() throws Exception {
        LoginRequest request = new LoginRequest("nao-existe@example.com", "whatever123");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsLoginWithInvalidPayload() throws Exception {
        LoginRequest request = new LoginRequest("", "");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}