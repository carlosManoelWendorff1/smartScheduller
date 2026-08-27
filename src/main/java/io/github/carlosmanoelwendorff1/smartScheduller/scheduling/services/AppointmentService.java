package io.github.carlosmanoelwendorff1.smartScheduller.scheduling.services;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.exception.AppointmentNotFoundException;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model.Appointment;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.repository.AppointmentRepository;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public Appointment create(UUID tenantId, UUID customerId, UUID serviceId, UUID professionalId, UUID resourceId,
            Instant startAt, Instant endAt, String notes) {
        Appointment appointment = Appointment.create(tenantId, customerId, serviceId, professionalId, resourceId,
                startAt, endAt, notes);
        // customerId/serviceId/professionalId/resourceId are backed by DB-level
        // foreign keys (see V7 migration) - same pattern as Customer -> Tenant.
        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public Appointment findById(UUID tenantId, UUID id) {
        return appointmentRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<Appointment> findAll(UUID tenantId, Pageable pageable) {
        return appointmentRepository.findAllByTenantId(tenantId, pageable);
    }

    public Appointment reschedule(UUID tenantId, UUID id, Instant newStart, Instant newEnd) {
        Appointment appointment = findById(tenantId, id);
        appointment.reschedule(newStart, newEnd);
        return appointment;
    }

    public Appointment updateNotes(UUID tenantId, UUID id, String notes) {
        Appointment appointment = findById(tenantId, id);
        appointment.updateNotes(notes);
        return appointment;
    }

    public Appointment confirm(UUID tenantId, UUID id) {
        Appointment appointment = findById(tenantId, id);
        appointment.confirm();
        return appointment;
    }

    public Appointment cancel(UUID tenantId, UUID id) {
        Appointment appointment = findById(tenantId, id);
        appointment.cancel();
        return appointment;
    }

    public Appointment complete(UUID tenantId, UUID id) {
        Appointment appointment = findById(tenantId, id);
        appointment.complete();
        return appointment;
    }

    public Appointment markNoShow(UUID tenantId, UUID id) {
        Appointment appointment = findById(tenantId, id);
        appointment.markNoShow();
        return appointment;
    }
}