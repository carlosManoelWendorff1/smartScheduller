package io.github.carlosmanoelwendorff1.smartScheduller.resource.controllers;

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
import io.github.carlosmanoelwendorff1.smartScheduller.identity.security.JwtService;
import io.github.carlosmanoelwendorff1.smartScheduller.resource.controllers.dto.CreateResourceRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.resource.controllers.dto.UpdateResourceRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class ResourceControllerIT extends AbstractIntegrationTest {

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
                Tenant.create("Tenant Resource " + UUID.randomUUID(), "tenant-resource-" + UUID.randomUUID(),
                        "America/Sao_Paulo"));
        token = "Bearer " + jwtService.generateToken(UUID.randomUUID(), tenant.getId(), "ADMIN");
    }

    @Test
    void createsResourceAndReturns201() throws Exception {
        CreateResourceRequest request = new CreateResourceRequest("Cadeira 1", "chair");

        mockMvc.perform(post("/api/v1/resources")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/resources/")))
                .andExpect(jsonPath("$.name", is("Cadeira 1")))
                .andExpect(jsonPath("$.type", is("chair")));
    }

    @Test
    void fullLifecycle_create_update_deactivate_activate() throws Exception {
        CreateResourceRequest request = new CreateResourceRequest("Cadeira 1", "chair");

        MvcResult createResult = mockMvc.perform(post("/api/v1/resources")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asString();

        mockMvc.perform(put("/api/v1/resources/{id}", id)
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new UpdateResourceRequest("Sala 2", "room"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("room")));

        mockMvc.perform(post("/api/v1/resources/{id}/deactivate", id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("INACTIVE")));

        mockMvc.perform(post("/api/v1/resources/{id}/activate", id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void returns404ForUnknownResource() throws Exception {
        mockMvc.perform(get("/api/v1/resources/{id}", UUID.randomUUID()).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void listsResourcesAsAPaginatedResponse() throws Exception {
        mockMvc.perform(post("/api/v1/resources")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new CreateResourceRequest("Cadeira 1", "chair"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/resources").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }
}