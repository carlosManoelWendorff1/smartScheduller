package io.github.carlosmanoelwendorff1.smartScheduller.tenant.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.carlosmanoelwendorff1.smartScheduller.AbstractIntegrationTest;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.controllers.dto.CreateTenantRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.controllers.dto.RenameTenantRequest;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class TenantControllerIT extends AbstractIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void createsTenantAndReturns201WithLocationHeader() throws Exception {
                CreateTenantRequest request = new CreateTenantRequest("Salao Beleza Pura", "salao-beleza-pura",
                                "America/Sao_Paulo");

                mockMvc.perform(post("/api/v1/tenants")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location", containsString("/api/v1/tenants/")))
                                .andExpect(jsonPath("$.id", notNullValue()))
                                .andExpect(jsonPath("$.name", is("Salao Beleza Pura")))
                                .andExpect(jsonPath("$.slug", is("salao-beleza-pura")))
                                .andExpect(jsonPath("$.status", is("ACTIVE")));
        }

        @Test
        void rejectsCreationWithInvalidPayload() throws Exception {
                CreateTenantRequest request = new CreateTenantRequest("", "", "");

                mockMvc.perform(post("/api/v1/tenants")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void rejectsDuplicatedSlugWithConflict() throws Exception {
                CreateTenantRequest request = new CreateTenantRequest("Consultorio A", "consultorio-unico",
                                "America/Sao_Paulo");

                mockMvc.perform(post("/api/v1/tenants")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());

                CreateTenantRequest duplicated = new CreateTenantRequest("Consultorio B", "consultorio-unico",
                                "America/Sao_Paulo");

                mockMvc.perform(post("/api/v1/tenants")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(duplicated)))
                                .andExpect(status().isConflict());
        }

        @Test
        void fullLifecycle_create_fetch_rename_suspend_activate_close() throws Exception {
                CreateTenantRequest request = new CreateTenantRequest("Academia Vida Ativa", "academia-vida-ativa",
                                "America/Sao_Paulo");

                MvcResult createResult = mockMvc.perform(post("/api/v1/tenants")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andReturn();

                String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asString();

                mockMvc.perform(get("/api/v1/tenants/{id}", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name", is("Academia Vida Ativa")));

                RenameTenantRequest renameRequest = new RenameTenantRequest("Academia Vida Ativa 2.0");
                mockMvc.perform(patch("/api/v1/tenants/{id}", id)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(renameRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name", is("Academia Vida Ativa 2.0")));

                mockMvc.perform(post("/api/v1/tenants/{id}/suspend", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("SUSPENDED")));

                mockMvc.perform(post("/api/v1/tenants/{id}/activate", id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status", is("ACTIVE")));

                mockMvc.perform(delete("/api/v1/tenants/{id}", id))
                                .andExpect(status().isNoContent());
        }

        @Test
        void returns404ForUnknownTenant() throws Exception {
                mockMvc.perform(get("/api/v1/tenants/{id}", "00000000-0000-0000-0000-000000000000"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void listsCreatedTenantsAsAPaginatedResponse() throws Exception {
                mockMvc.perform(post("/api/v1/tenants")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(
                                                new CreateTenantRequest("Barbearia Listagem", "barbearia-listagem",
                                                                "America/Sao_Paulo"))))
                                .andExpect(status().isCreated());

                mockMvc.perform(get("/api/v1/tenants"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                                .andExpect(jsonPath("$.page", is(0)))
                                .andExpect(jsonPath("$.size", is(20)))
                                .andExpect(jsonPath("$.totalElements").exists())
                                .andExpect(jsonPath("$.totalPages").exists());
        }

        @Test
        void listRespectsPageAndSizeQueryParams() throws Exception {
                for (int i = 0; i < 3; i++) {
                        mockMvc.perform(post("/api/v1/tenants")
                                        .contentType("application/json")
                                        .content(objectMapper.writeValueAsString(
                                                        new CreateTenantRequest("Tenant Pag " + i, "tenant-pag-" + i,
                                                                        "America/Sao_Paulo"))))
                                        .andExpect(status().isCreated());
                }

                mockMvc.perform(get("/api/v1/tenants").param("page", "0").param("size", "2"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(2)))
                                .andExpect(jsonPath("$.size", is(2)))
                                .andExpect(jsonPath("$.first", is(true)));
        }
}