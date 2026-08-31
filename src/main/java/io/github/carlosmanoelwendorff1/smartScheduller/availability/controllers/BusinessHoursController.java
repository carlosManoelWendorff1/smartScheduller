package io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers.dto.BusinessHoursResponse;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.controllers.dto.UpsertBusinessHoursRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.services.BusinessHoursService;
import io.github.carlosmanoelwendorff1.smartScheduller.common.TenantContext;

@RestController
@RequestMapping("/api/v1/business-hours")
public class BusinessHoursController {

    private final BusinessHoursService businessHoursService;
    private final TenantContext tenantContext;

    public BusinessHoursController(BusinessHoursService businessHoursService, TenantContext tenantContext) {
        this.businessHoursService = businessHoursService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    public List<BusinessHoursResponse> findAll() {
        return businessHoursService.findAll(tenantContext.currentTenantId()).stream()
                .map(BusinessHoursResponse::from)
                .toList();
    }

    @PutMapping("/{dayOfWeek}")
    public BusinessHoursResponse upsert(@PathVariable String dayOfWeek,
            @RequestBody UpsertBusinessHoursRequest request) {
        DayOfWeek day = parseDayOfWeek(dayOfWeek);
        var businessHours = businessHoursService.upsert(tenantContext.currentTenantId(), day, request.startTime(),
                request.endTime(), request.closed());
        return BusinessHoursResponse.from(businessHours);
    }

    @DeleteMapping("/{dayOfWeek}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String dayOfWeek) {
        businessHoursService.delete(tenantContext.currentTenantId(), parseDayOfWeek(dayOfWeek));
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