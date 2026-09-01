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
import io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers.dto.UpsertBusinessHoursRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.security.JwtService;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class BusinessHoursControllerIT extends AbstractIntegrationTest {

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
                Tenant.create("Tenant BH " + UUID.randomUUID(), "tenant-bh-" + UUID.randomUUID(), "America/Sao_Paulo"));
        token = "Bearer " + jwtService.generateToken(UUID.randomUUID(), tenant.getId(), "ADMIN");
    }

    @Test
    void rejectsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/business-hours"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void upsertsCreatesThenUpdatesTheSameDay() throws Exception {
        UpsertBusinessHoursRequest create = new UpsertBusinessHoursRequest(LocalTime.of(9, 0), LocalTime.of(18, 0),
                false);

        mockMvc.perform(put("/api/v1/business-hours/{day}", "MONDAY")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dayOfWeek", is("MONDAY")))
                .andExpect(jsonPath("$.startTime", is("09:00:00")));

        UpsertBusinessHoursRequest update = new UpsertBusinessHoursRequest(LocalTime.of(10, 0), LocalTime.of(16, 0),
                false);

        mockMvc.perform(put("/api/v1/business-hours/{day}", "MONDAY")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime", is("10:00:00")));

        mockMvc.perform(get("/api/v1/business-hours").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void rejectsInvalidDayOfWeek() throws Exception {
        UpsertBusinessHoursRequest request = new UpsertBusinessHoursRequest(LocalTime.of(9, 0), LocalTime.of(18, 0),
                false);

        mockMvc.perform(put("/api/v1/business-hours/{day}", "FUNDAY")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsOpenDayWithoutTimes() throws Exception {
        UpsertBusinessHoursRequest request = new UpsertBusinessHoursRequest(null, null, false);

        mockMvc.perform(put("/api/v1/business-hours/{day}", "MONDAY")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletesADay() throws Exception {
        UpsertBusinessHoursRequest request = new UpsertBusinessHoursRequest(LocalTime.of(9, 0), LocalTime.of(18, 0),
                false);
        mockMvc.perform(put("/api/v1/business-hours/{day}", "MONDAY")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/business-hours/{day}", "MONDAY").header("Authorization", token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/business-hours").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}