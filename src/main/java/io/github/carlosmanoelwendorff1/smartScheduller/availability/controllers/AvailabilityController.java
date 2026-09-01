package io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers.dto.AvailabilityResponse;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.services.AvailabilityService;
import io.github.carlosmanoelwendorff1.smartScheduller.common.TenantContext;

@RestController
@RequestMapping("/api/v1/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private final TenantContext tenantContext;

    public AvailabilityController(AvailabilityService availabilityService, TenantContext tenantContext) {
        this.availabilityService = availabilityService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public AvailabilityResponse findAvailability(
            @RequestParam UUID professionalId,
            @RequestParam(required = false) UUID resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam int durationMinutes,
            @RequestParam(required = false) Integer slotIntervalMinutes) {
        var slots = availabilityService.findAvailableSlots(tenantContext.currentTenantId(), professionalId,
                resourceId, date, durationMinutes, slotIntervalMinutes);
        return AvailabilityResponse.from(professionalId, resourceId, date, slots);
    }
}