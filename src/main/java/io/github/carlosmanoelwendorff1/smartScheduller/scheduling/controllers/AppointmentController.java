package io.github.carlosmanoelwendorff1.smartScheduller.scheduling.controllers;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.carlosmanoelwendorff1.smartScheduller.common.PageResponse;
import io.github.carlosmanoelwendorff1.smartScheduller.common.TenantContext;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.controllers.dto.AppointmentResponse;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.controllers.dto.CreateAppointmentRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.controllers.dto.RescheduleAppointmentRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.controllers.dto.UpdateAppointmentNotesRequest;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model.Appointment;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.services.AppointmentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final TenantContext tenantContext;

    public AppointmentController(AppointmentService appointmentService, TenantContext tenantContext) {
        this.appointmentService = appointmentService;
        this.tenantContext = tenantContext;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request) {
        Appointment appointment = appointmentService.create(tenantContext.currentTenantId(), request.customerId(),
                request.serviceId(), request.professionalId(), request.resourceId(), request.startAt(),
                request.endAt(), request.notes());
        AppointmentResponse response = AppointmentResponse.from(appointment);
        return ResponseEntity.created(URI.create("/api/v1/appointments/" + response.id())).body(response);
    }

    @GetMapping
    public PageResponse<AppointmentResponse> findAll(
            @PageableDefault(size = 20, sort = "startAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return PageResponse.from(
                appointmentService.findAll(tenantContext.currentTenantId(), pageable).map(AppointmentResponse::from));
    }

    @GetMapping("/{id}")
    public AppointmentResponse findById(@PathVariable UUID id) {
        return AppointmentResponse.from(appointmentService.findById(tenantContext.currentTenantId(), id));
    }

    @PutMapping("/{id}/reschedule")
    public AppointmentResponse reschedule(@PathVariable UUID id,
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        return AppointmentResponse.from(
                appointmentService.reschedule(tenantContext.currentTenantId(), id, request.startAt(), request.endAt()));
    }

    @PatchMapping("/{id}/notes")
    public AppointmentResponse updateNotes(@PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentNotesRequest request) {
        return AppointmentResponse
                .from(appointmentService.updateNotes(tenantContext.currentTenantId(), id, request.notes()));
    }

    @PostMapping("/{id}/confirm")
    public AppointmentResponse confirm(@PathVariable UUID id) {
        return AppointmentResponse.from(appointmentService.confirm(tenantContext.currentTenantId(), id));
    }

    @PostMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable UUID id) {
        return AppointmentResponse.from(appointmentService.cancel(tenantContext.currentTenantId(), id));
    }

    @PostMapping("/{id}/complete")
    public AppointmentResponse complete(@PathVariable UUID id) {
        return AppointmentResponse.from(appointmentService.complete(tenantContext.currentTenantId(), id));
    }

    @PostMapping("/{id}/no-show")
    public AppointmentResponse markNoShow(@PathVariable UUID id) {
        return AppointmentResponse.from(appointmentService.markNoShow(tenantContext.currentTenantId(), id));
    }
}