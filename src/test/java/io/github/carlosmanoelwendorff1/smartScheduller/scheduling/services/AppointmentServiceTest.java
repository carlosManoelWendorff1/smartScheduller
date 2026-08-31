package io.github.carlosmanoelwendorff1.smartScheduller.scheduling.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.exception.AppointmentConflictException;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model.Appointment;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model.AppointmentStatus;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.repository.AppointmentRepository;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    private AppointmentService appointmentService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final UUID serviceId = UUID.randomUUID();
    private final UUID professionalId = UUID.randomUUID();
    private final Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
    private final Instant end = start.plus(30, ChronoUnit.MINUTES);

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentService(appointmentRepository);
    }

    @Test
    void createsAppointmentWhenNoConflict() {
        when(appointmentRepository.findOverlapping(eq(tenantId), any(), eq(professionalId), isNull(), eq(start),
                eq(end), anyList())).thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        Appointment appointment = appointmentService.create(tenantId, customerId, serviceId, professionalId, null,
                start, end, null);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void rejectsCreationWhenProfessionalHasOverlappingAppointment() {
        Appointment existing = Appointment.create(tenantId, UUID.randomUUID(), serviceId, professionalId, null,
                start, end, null);
        when(appointmentRepository.findOverlapping(eq(tenantId), any(), eq(professionalId), isNull(), eq(start),
                eq(end), anyList())).thenReturn(List.of(existing));

        assertThatThrownBy(() -> appointmentService.create(tenantId, customerId, serviceId, professionalId, null,
                start, end, null))
                .isInstanceOf(AppointmentConflictException.class);

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void skipsConflictCheckWhenNeitherProfessionalNorResourceIsSet() {
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        appointmentService.create(tenantId, customerId, serviceId, null, null, start, end, null);

        verify(appointmentRepository, never()).findOverlapping(any(), any(), any(), any(), any(), any(), any());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void rejectsRescheduleWhenNewTimeConflicts() {
        Appointment appointment = Appointment.create(tenantId, customerId, serviceId, professionalId, null, start,
                end, null);
        Appointment other = Appointment.create(tenantId, UUID.randomUUID(), serviceId, professionalId, null,
                start.plus(1, ChronoUnit.DAYS), end.plus(1, ChronoUnit.DAYS), null);

        when(appointmentRepository.findByIdAndTenantId(appointment.getId(), tenantId))
                .thenReturn(Optional.of(appointment));
        Instant newStart = start.plus(1, ChronoUnit.DAYS);
        Instant newEnd = end.plus(1, ChronoUnit.DAYS);
        when(appointmentRepository.findOverlapping(eq(tenantId), eq(appointment.getId()), eq(professionalId),
                isNull(), eq(newStart), eq(newEnd), anyList())).thenReturn(List.of(other));

        assertThatThrownBy(() -> appointmentService.reschedule(tenantId, appointment.getId(), newStart, newEnd))
                .isInstanceOf(AppointmentConflictException.class);
    }
}