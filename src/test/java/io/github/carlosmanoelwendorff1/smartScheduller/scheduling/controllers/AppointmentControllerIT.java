package io.github.carlosmanoelwendorff1.smartScheduller.scheduling.controllers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.service.Service;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.repository.service.ServiceRepository;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.model.Customer;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.repository.CustomerRepository;
import io.github.carlosmanoelwendorff1.smartScheduller.identity.security.JwtService;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.controllers.dto.CreateAppointmentRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.controllers.dto.RescheduleAppointmentRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class AppointmentControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private JwtService jwtService;

    private String token;
    private UUID customerId;
    private UUID serviceId;

    @BeforeEach
    void setUp() {
        Tenant tenant = tenantRepository.saveAndFlush(
                Tenant.create("Tenant Appt " + UUID.randomUUID(), "tenant-appt-" + UUID.randomUUID(),
                        "America/Sao_Paulo"));
        token = "Bearer " + jwtService.generateToken(UUID.randomUUID(), tenant.getId(), "ADMIN");

        customerId = customerRepository.saveAndFlush(
                Customer.create(tenant.getId(), "Cliente", null, null, null, null)).getId();
        serviceId = serviceRepository.saveAndFlush(
                Service.create(tenant.getId(), "Servico", null, 30, new BigDecimal("50.00"))).getId();
    }

    private CreateAppointmentRequest newRequest() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        return new CreateAppointmentRequest(customerId, serviceId, null, null, start, start.plusSeconds(1800), null);
    }

    @Test
    void createsAppointmentAndReturns201() throws Exception {
        mockMvc.perform(post("/api/v1/appointments")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(newRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/appointments/")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.customerId", is(customerId.toString())));
    }

    @Test
    void rejectsUnknownCustomerWithBadRequest() throws Exception {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        CreateAppointmentRequest request = new CreateAppointmentRequest(UUID.randomUUID(), serviceId, null, null,
                start, start.plusSeconds(1800), null);

        mockMvc.perform(post("/api/v1/appointments")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fullLifecycle_create_confirm_reschedule_complete() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/appointments")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(newRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asString();

        mockMvc.perform(post("/api/v1/appointments/{id}/confirm", id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMED")));

        Instant newStart = Instant.now().plus(2, ChronoUnit.DAYS);
        RescheduleAppointmentRequest rescheduleRequest = new RescheduleAppointmentRequest(newStart,
                newStart.plusSeconds(1800));
        mockMvc.perform(put("/api/v1/appointments/{id}/reschedule", id)
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(rescheduleRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/appointments/{id}/complete", id).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    void cannotCompleteAPendingAppointment() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/appointments")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(newRequest())))
                .andExpect(status().isCreated())
                .andReturn();

        String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asString();

        mockMvc.perform(post("/api/v1/appointments/{id}/complete", id).header("Authorization", token))
                .andExpect(status().isConflict());
    }

    @Test
    void returns404ForUnknownAppointment() throws Exception {
        mockMvc.perform(get("/api/v1/appointments/{id}", UUID.randomUUID()).header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    void listsAppointmentsAsAPaginatedResponse() throws Exception {
        mockMvc.perform(post("/api/v1/appointments")
                .header("Authorization", token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(newRequest())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/appointments").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }
}