package io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model.Appointment;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model.AppointmentStatus;

class AppointmentTest {

    private final UUID tenantId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final UUID serviceId = UUID.randomUUID();
    private final Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
    private final Instant end = start.plus(30, ChronoUnit.MINUTES);

    @Test
    void createsPendingAppointment() {
        Appointment appointment = Appointment.create(tenantId, customerId, serviceId, null, null, start, end, null);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.PENDING);
    }

    @Test
    void rejectsEndBeforeStart() {
        assertThatThrownBy(() -> Appointment.create(tenantId, customerId, serviceId, null, null, end, start, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void confirmMovesFromPendingToConfirmed() {
        Appointment appointment = Appointment.create(tenantId, customerId, serviceId, null, null, start, end, null);

        appointment.confirm();

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    void cannotCompleteAPendingAppointment() {
        Appointment appointment = Appointment.create(tenantId, customerId, serviceId, null, null, start, end, null);

        assertThatThrownBy(appointment::complete).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void completeRequiresConfirmedFirst() {
        Appointment appointment = Appointment.create(tenantId, customerId, serviceId, null, null, start, end, null);
        appointment.confirm();

        appointment.complete();

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
    }

    @Test
    void cannotCancelACompletedAppointment() {
        Appointment appointment = Appointment.create(tenantId, customerId, serviceId, null, null, start, end, null);
        appointment.confirm();
        appointment.complete();

        assertThatThrownBy(appointment::cancel).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rescheduleUpdatesTimesWhilePending() {
        Appointment appointment = Appointment.create(tenantId, customerId, serviceId, null, null, start, end, null);
        Instant newStart = start.plus(1, ChronoUnit.DAYS);
        Instant newEnd = newStart.plus(30, ChronoUnit.MINUTES);

        appointment.reschedule(newStart, newEnd);

        assertThat(appointment.getStartAt()).isEqualTo(newStart);
    }
}