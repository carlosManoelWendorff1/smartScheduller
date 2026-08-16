package io.github.carlosmanoelwendorff1.smartScheduller.tenant.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(

        @NotBlank(message = "O nome e obrigatorio.") @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres.") String name,

        @NotBlank(message = "O slug e obrigatorio.") @Size(max = 80, message = "O slug deve ter no maximo 80 caracteres.") String slug,

        @NotBlank(message = "O timezone e obrigatorio.") String timezone) {
}
