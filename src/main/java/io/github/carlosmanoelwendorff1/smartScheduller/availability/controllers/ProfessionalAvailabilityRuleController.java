package io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers.dto.ProfessionalAvailabilityRuleResponse;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers.dto.UpsertAvailabilityRuleRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.services.ProfessionalAvailabilityRuleService;
import io.github.carlosmanoelwendorff1.smartScheduller.common.TenantContext;

@RestController
@RequestMapping("/api/v1/professionals/{professionalId}/availability-rules")
public class ProfessionalAvailabilityRuleController {

    private final ProfessionalAvailabilityRuleService ruleService;
    private final TenantContext tenantContext;

    public ProfessionalAvailabilityRuleController(ProfessionalAvailabilityRuleService ruleService,
            TenantContext tenantContext) {
        this.ruleService = ruleService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public List<ProfessionalAvailabilityRuleResponse> findAll(@PathVariable UUID professionalId) {
        return ruleService.findAll(tenantContext.currentTenantId(), professionalId).stream()
                .map(ProfessionalAvailabilityRuleResponse::from)
                .toList();
    }

    @PutMapping("/{dayOfWeek}")
    public ProfessionalAvailabilityRuleResponse upsert(@PathVariable UUID professionalId,
            @PathVariable String dayOfWeek,
            @RequestBody UpsertAvailabilityRuleRequest request) {
        DayOfWeek day = parseDayOfWeek(dayOfWeek);
        var rule = ruleService.upsert(tenantContext.currentTenantId(), professionalId, day, request.startTime(),
                request.endTime(), request.closed());
        return ProfessionalAvailabilityRuleResponse.from(rule);
    }

    @DeleteMapping("/{dayOfWeek}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID professionalId, @PathVariable String dayOfWeek) {
        ruleService.delete(tenantContext.currentTenantId(), professionalId, parseDayOfWeek(dayOfWeek));
    }

    private DayOfWeek parseDayOfWeek(String value) {
        try {
            return DayOfWeek.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid dayOfWeek: " + value + ". Expected one of MONDAY..SUNDAY.");
        }
    }
}