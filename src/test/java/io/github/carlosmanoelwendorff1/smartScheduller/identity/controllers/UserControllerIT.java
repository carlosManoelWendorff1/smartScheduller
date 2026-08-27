package io.github.carlosmanoelwendorff1.smartScheduller.identity.controllers;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.carlosmanoelwendorff1.smartScheduller.AbstractIntegrationTest;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.controllers.dto.RegisterUserRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.domain.model.Role;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class UserControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        Tenant tenant = tenantRepository.saveAndFlush(
                Tenant.create("Clinica User " + UUID.randomUUID(), "clinica-user-" + UUID.randomUUID(),
                        "America/Sao_Paulo"));
        tenantId = tenant.getId();
    }

    @Test
    void registersUserAndReturns201WithLocationHeader() throws Exception {
        // TEMPORARY: endpoint is open (bootstrap problem) - no token here on purpose.
        RegisterUserRequest request = new RegisterUserRequest("Admin Root", "admin@example.com", "supersecret123",
                Role.ADMIN);

        mockMvc.perform(post("/api/v1/tenants/{tenantId}/users", tenantId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/users/")))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Admin Root")))
                .andExpect(jsonPath("$.email", is("admin@example.com")))
                .andExpect(jsonPath("$.role", is("ADMIN")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                // password/passwordHash must never appear in the response
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void rejectsRegistrationWithInvalidPayload() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest("", "not-an-email", "short", null);

        mockMvc.perform(post("/api/v1/tenants/{tenantId}/users", tenantId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsDuplicatedEmailGlobally() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest("Recepcionista", "duplicado@example.com",
                "supersecret123", Role.RECEPTIONIST);

        mockMvc.perform(post("/api/v1/tenants/{tenantId}/users", tenantId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Even on a *different* tenant - email is globally unique on purpose.
        Tenant otherTenant = tenantRepository.saveAndFlush(
                Tenant.create("Outro Tenant " + UUID.randomUUID(), "outro-user-" + UUID.randomUUID(),
                        "America/Sao_Paulo"));

        RegisterUserRequest duplicated = new RegisterUserRequest("Outro Nome", "duplicado@example.com",
                "supersecret123", Role.RECEPTIONIST);

        mockMvc.perform(post("/api/v1/tenants/{tenantId}/users", otherTenant.getId())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(duplicated)))
                .andExpect(status().isConflict());
    }
}