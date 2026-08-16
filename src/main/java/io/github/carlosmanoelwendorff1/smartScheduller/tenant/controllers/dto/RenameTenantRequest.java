package io.github.carlosmanoelwendorff1.smartScheduller.tenant.controllers.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameTenantRequest(

        @NotBlank(message = "O nome e obrigatorio.") @Size(max = 150, message = "O nome deve ter no maximo 150 caracteres.") String name) {
}
