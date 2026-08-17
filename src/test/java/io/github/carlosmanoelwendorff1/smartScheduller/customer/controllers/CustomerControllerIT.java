package io.github.carlosmanoelwendorff1.smartScheduller.customer.controllers;

import java.time.LocalDate;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.carlosmanoelwendorff1.smartScheduller.AbstractIntegrationTest;
import io.github.carlosmanoelwendorff1.smartScheduller.common.context.HeaderTenantContext;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.controllers.dto.CreateCustomerRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.controllers.dto.UpdateCustomerProfileRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class CustomerControllerIT extends AbstractIntegrationTest {

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
                Tenant.create("Clinica Teste " + UUID.randomUUID(), "clinica-teste-" + UUID.randomUUID(),
                        "America/Sao_Paulo"));
        tenantId = tenant.getId();
    }

    @Test
    void createsCustomerAndReturns201WithLocationHeader() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest("Maria Silva", "maria@example.com",
                "+55 47 99999-0000", "12345678900", LocalDate.of(1990, 5, 20));

        mockMvc.perform(post("/api/v1/customers")
                .header(HeaderTenantContext.TENANT_HEADER, tenantId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/customers/")))
                .andExpect(jsonPath("$.name", is("Maria Silva")))
                .andExpect(jsonPath("$.email", is("maria@example.com")))
                .andExpect(jsonPath("$.status", is("ACTIVE")))
                .andExpect(jsonPath("$.tenantId", is(tenantId.toString())));
    }

    @Test
    void rejectsRequestWithoutTenantHeader() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest("Maria Silva", null, null, null, null);

        mockMvc.perform(post("/api/v1/customers")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsCreationWithInvalidPayload() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest("", "not-an-email", null, null,
                LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/v1/customers")
                .header(HeaderTenantContext.TENANT_HEADER, tenantId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsDuplicatedEmailWithinSameTenant() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest("Cliente A", "duplicado@example.com", null, null,
                null);

        mockMvc.perform(post("/api/v1/customers")
                .header(HeaderTenantContext.TENANT_HEADER, tenantId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        CreateCustomerRequest duplicated = new CreateCustomerRequest("Cliente B", "duplicado@example.com", null,
                null, null);

        mockMvc.perform(post("/api/v1/customers")
                .header(HeaderTenantContext.TENANT_HEADER, tenantId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(duplicated)))
                .andExpect(status().isConflict());
    }

    @Test
    void returns400ForUnknownTenantDueToForeignKeyViolation() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest("Cliente Orfao", null, null, null, null);

        mockMvc.perform(post("/api/v1/customers")
                .header(HeaderTenantContext.TENANT_HEADER, UUID.randomUUID())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fullLifecycle_create_fetch_rename_updateProfile_archive_activate() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest("Joao Pereira", null, null, null, null);

        MvcResult createResult = mockMvc.perform(post("/api/v1/customers")
                .header(HeaderTenantContext.TENANT_HEADER, tenantId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String customerId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asString();

        mockMvc.perform(get("/api/v1/customers/{id}", customerId)
                .header(HeaderTenantContext.TENANT_HEADER, tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Joao Pereira")));

        mockMvc.perform(patch("/api/v1/customers/{id}", customerId)
                .header(HeaderTenantContext.TENANT_HEADER, tenantId)
                .contentType("application/json")
                .content("{\"name\":\"Joao P. Souza\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Joao P. Souza")));

        UpdateCustomerProfileRequest profileRequest = new UpdateCustomerProfileRequest("joao@example.com",
                "47999990000", "doc-1", LocalDate.of(1985, 3, 10));
        mockMvc.perform(put("/api/v1/customers/{id}/profile", customerId)
                .header(HeaderTenantContext.TENANT_HEADER, tenantId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(profileRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("joao@example.com")));

        mockMvc.perform(post("/api/v1/customers/{id}/archive", customerId)
                .header(HeaderTenantContext.TENANT_HEADER, tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("INACTIVE")));

        mockMvc.perform(post("/api/v1/customers/{id}/activate", customerId)
                .header(HeaderTenantContext.TENANT_HEADER, tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void returns404WhenCustomerBelongsToAnotherTenant() throws Exception {
        Tenant otherTenant = tenantRepository.saveAndFlush(
                Tenant.create("Outro Tenant " + UUID.randomUUID(), "outro-tenant-" + UUID.randomUUID(),
                        "America/Sao_Paulo"));

        CreateCustomerRequest request = new CreateCustomerRequest("Cliente Isolado", null, null, null, null);
        MvcResult createResult = mockMvc.perform(post("/api/v1/customers")
                .header(HeaderTenantContext.TENANT_HEADER, tenantId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String customerId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asString();

        mockMvc.perform(get("/api/v1/customers/{id}", customerId)
                .header(HeaderTenantContext.TENANT_HEADER, otherTenant.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void listsCustomersAsAPaginatedResponseScopedToTenant() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                .header(HeaderTenantContext.TENANT_HEADER, tenantId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(
                        new CreateCustomerRequest("Cliente Listagem", null, null, null, null))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/customers")
                .header(HeaderTenantContext.TENANT_HEADER, tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].tenantId", is(tenantId.toString())));
    }
}