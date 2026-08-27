// catalog/controllers/ProfessionalControllerIT.java
package io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.professional;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.carlosmanoelwendorff1.smartScheduller.AbstractIntegrationTest;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.professional.dto.CreateProfessionalRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.controllers.professional.dto.RenameProfessionalRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.security.JwtService;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class ProfessionalControllerIT extends AbstractIntegrationTest {

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
                Tenant.create("Tenant Prof " + UUID.randomUUID(), "tenant-prof-" + UUID.randomUUID(),
                        "America/Sao_Paulo"));
        token = "Bearer " + jwtService.generateToken(UUID.randomUUID(), tenant.getId(), "ADMIN");
    }

    @Test
    void createsProfessionalAndReturns201() throws Exception {
        CreateProfessionalRequest request = new CreateProfessionalRequest("Dra. Ana", null);

        mockMvc.perform(post("/api/v1/professionals")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/professionals/")))
                .andExpect(jsonPath("$.name", is("Dra. Ana")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void fullLifecycle_create_rename_deactivate_activate() throws Exception {
        CreateProfessionalRequest request = new CreateProfessionalRequest("Dra. Ana", null);

        MvcResult createResult = mockMvc.perform(post("/api/v1/professionals")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asString();

        mockMvc.perform(patch("/api/v1/professionals/{id}", id)
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new RenameProfessionalRequest("Dra. Ana Souza"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Dra. Ana Souza")));

        mockMvc.perform(post("/api/v1/professionals/{id}/deactivate", id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("INACTIVE")));

        mockMvc.perform(post("/api/v1/professionals/{id}/activate", id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void returns404ForUnknownProfessional() throws Exception {
        mockMvc.perform(get("/api/v1/professionals/{id}", UUID.randomUUID()).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void listsProfessionalsAsAPaginatedResponse() throws Exception {
        mockMvc.perform(post("/api/v1/professionals")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new CreateProfessionalRequest("Dra. Ana", null))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/professionals").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }
}