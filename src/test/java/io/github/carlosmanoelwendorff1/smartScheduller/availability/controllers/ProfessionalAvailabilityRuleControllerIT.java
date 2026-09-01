package io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers;

import java.time.LocalTime;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.carlosmanoelwendorff1.smartScheduller.AbstractIntegrationTest;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers.dto.UpsertAvailabilityRuleRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.professional.Professional;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.repository.professional.ProfessionalRepository;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.security.JwtService;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class ProfessionalAvailabilityRuleControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ProfessionalRepository professionalRepository;

    @Autowired
    private JwtService jwtService;

    private String token;
    private UUID professionalId;

    @BeforeEach
    void setUp() {
        Tenant tenant = tenantRepository.saveAndFlush(
                Tenant.create("Tenant Rule " + UUID.randomUUID(), "tenant-rule-" + UUID.randomUUID(),
                        "America/Sao_Paulo"));
        token = "Bearer " + jwtService.generateToken(UUID.randomUUID(), tenant.getId(), "ADMIN");
        professionalId = professionalRepository.saveAndFlush(Professional.create(tenant.getId(), "Dra. Ana", null))
                .getId();
    }

    @Test
    void upsertsARuleForAProfessional() throws Exception {
        UpsertAvailabilityRuleRequest request = new UpsertAvailabilityRuleRequest(LocalTime.of(8, 0),
                LocalTime.of(17, 0), false);

        mockMvc.perform(put("/api/v1/professionals/{id}/availability-rules/{day}", professionalId, "WEDNESDAY")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayOfWeek", is("WEDNESDAY")))
                .andExpect(jsonPath("$.professionalId", is(professionalId.toString())));
    }

    @Test
    void rejectsRuleForProfessionalFromAnotherTenant() throws Exception {
        Tenant otherTenant = tenantRepository.saveAndFlush(
                Tenant.create("Outro Tenant", "tenant-rule-other-" + UUID.randomUUID(), "America/Sao_Paulo"));
        String otherToken = "Bearer " + jwtService.generateToken(UUID.randomUUID(), otherTenant.getId(), "ADMIN");

        UpsertAvailabilityRuleRequest request = new UpsertAvailabilityRuleRequest(LocalTime.of(8, 0),
                LocalTime.of(17, 0), false);

        // otherToken's tenant doesn't own `professionalId` - composite FK must reject
        // this.
        mockMvc.perform(put("/api/v1/professionals/{id}/availability-rules/{day}", professionalId, "WEDNESDAY")
                .header("Authorization", otherToken)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listsRulesForAProfessional() throws Exception {
        UpsertAvailabilityRuleRequest monday = new UpsertAvailabilityRuleRequest(LocalTime.of(8, 0),
                LocalTime.of(17, 0), false);
        UpsertAvailabilityRuleRequest tuesday = new UpsertAvailabilityRuleRequest(LocalTime.of(9, 0),
                LocalTime.of(18, 0), false);

        mockMvc.perform(put("/api/v1/professionals/{id}/availability-rules/{day}", professionalId, "MONDAY")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(monday)))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/professionals/{id}/availability-rules/{day}", professionalId, "TUESDAY")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(tuesday)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/professionals/{id}/availability-rules", professionalId)
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void deletesARule() throws Exception {
        UpsertAvailabilityRuleRequest request = new UpsertAvailabilityRuleRequest(LocalTime.of(8, 0),
                LocalTime.of(17, 0), false);
        mockMvc.perform(put("/api/v1/professionals/{id}/availability-rules/{day}", professionalId, "MONDAY")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/professionals/{id}/availability-rules/{day}", professionalId, "MONDAY")
                .header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/professionals/{id}/availability-rules", professionalId)
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}