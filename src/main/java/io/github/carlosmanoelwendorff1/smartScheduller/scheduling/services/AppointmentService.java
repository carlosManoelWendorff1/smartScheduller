// scheduling/services/AppointmentService.java
package io.github.carlosmanoelwendorff1.smartScheduller.scheduling.services;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.exception.AppointmentConflictException;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.exception.AppointmentNotFoundException;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model.Appointment;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model.AppointmentStatus;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.repository.AppointmentRepository;

@Service
@Transactional
public class AppointmentService {

    private static final List<AppointmentStatus> ACTIVE_STATUSES = List.of(AppointmentStatus.PENDING,
            AppointmentStatus.CONFIRMED);

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public Appointment create(UUID tenantId, UUID customerId, UUID serviceId, UUID professionalId, UUID resourceId,
            Instant startAt, Instant endAt, String notes) {
        Appointment appointment = Appointment.create(tenantId, customerId, serviceId, professionalId, resourceId,
                startAt, endAt, notes);

        assertNoConflict(tenantId, appointment.getId(), professionalId, resourceId, startAt, endAt);

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

        assertNoConflict(tenantId, id, appointment.getProfessionalId(), appointment.getResourceId(), newStart, newEnd);

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

    /**
     * If neither professionalId nor resourceId is set, there's nothing to
     * conflict against - the appointment is only tied to a customer/service.
     */
    private void assertNoConflict(UUID tenantId, UUID excludeId, UUID professionalId, UUID resourceId,
            Instant startAt, Instant endAt) {
        if (professionalId == null && resourceId == null) {
            return;
        }

        List<Appointment> conflicts = appointmentRepository.findOverlapping(tenantId, excludeId, professionalId,
                resourceId, startAt, endAt, ACTIVE_STATUSES);

        if (!conflicts.isEmpty()) {
            throw new AppointmentConflictException(
                    "The professional or resource is already booked for an overlapping time slot.");
        }
    }
}