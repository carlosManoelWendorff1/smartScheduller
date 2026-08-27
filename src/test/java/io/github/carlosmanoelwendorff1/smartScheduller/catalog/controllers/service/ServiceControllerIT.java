package io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.service;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.carlosmanoelwendorff1.smartScheduller.AbstractIntegrationTest;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.service.dto.CreateServiceRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.service.dto.UpdateServiceRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.security.JwtService;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class ServiceControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private JwtService jwtService;

    private String token;

    @BeforeEach
    void setUp() {
        Tenant tenant = tenantRepository.saveAndFlush(
                Tenant.create("Tenant Service " + UUID.randomUUID(), "tenant-service-" + UUID.randomUUID(),
                        "America/Sao_Paulo"));
        token = "Bearer " + jwtService.generateToken(UUID.randomUUID(), tenant.getId(), "ADMIN");
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createsServiceAndReturns201() throws Exception {
        CreateServiceRequest request = new CreateServiceRequest("Corte", "Corte tradicional", 30,
                new BigDecimal("50.00"));

        mockMvc.perform(post("/api/v1/services")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/services/")))
                .andExpect(jsonPath("$.name", is("Corte")))
                .andExpect(jsonPath("$.durationMinutes", is(30)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void rejectsInvalidPayload() throws Exception {
        CreateServiceRequest request = new CreateServiceRequest("", null, 0, new BigDecimal("-1"));

        mockMvc.perform(post("/api/v1/services")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fullLifecycle_create_fetch_update_deactivate_activate() throws Exception {
        CreateServiceRequest request = new CreateServiceRequest("Corte", null, 30, new BigDecimal("50.00"));

        MvcResult createResult = mockMvc.perform(post("/api/v1/services")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asString();

        mockMvc.perform(get("/api/v1/services/{id}", id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Corte")));

        UpdateServiceRequest updateRequest = new UpdateServiceRequest("Corte Premium", "Novo", 45,
                new BigDecimal("80.00"));
        mockMvc.perform(put("/api/v1/services/{id}", id)
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Corte Premium")))
                .andExpect(jsonPath("$.durationMinutes", is(45)));

        mockMvc.perform(post("/api/v1/services/{id}/deactivate", id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("INACTIVE")));

        mockMvc.perform(post("/api/v1/services/{id}/activate", id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void returns404ForUnknownService() throws Exception {
        mockMvc.perform(get("/api/v1/services/{id}", UUID.randomUUID()).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void listsServicesAsAPaginatedResponse() throws Exception {
        mockMvc.perform(post("/api/v1/services")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(
                        new CreateServiceRequest("Corte", null, 30, BigDecimal.TEN))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/services").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }
}