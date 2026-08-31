package io.github.carlosmanoelwendorff1.smartScheduller.scheduling.infrastructure;

import io.github.carlosmanoelwendorff1.smartScheduller.AbstractIntegrationTest;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.model.Customer;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.repository.CustomerRepository;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model.Appointment;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model.AppointmentStatus;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.repository.AppointmentRepository;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.service.Service;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.repository.service.ServiceRepository;

class AppointmentRepositoryConflictIT extends AbstractIntegrationTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Test
    void findOverlappingDetectsSameProfessionalOverlap() {
        Tenant tenant = tenantRepository
                .saveAndFlush(Tenant.create("Tenant Conflict", "conflict-a", "America/Sao_Paulo"));
        Customer customer = customerRepository
                .saveAndFlush(Customer.create(tenant.getId(), "Cliente", null, null, null, null));
        Service service = serviceRepository
                .saveAndFlush(Service.create(tenant.getId(), "Servico", null, 30, new BigDecimal("50.00")));
        UUID professionalId = UUID.randomUUID();

        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(30, ChronoUnit.MINUTES);
        Appointment existing = Appointment.create(tenant.getId(), customer.getId(), service.getId(), professionalId,
                null, start, end, null);
        appointmentRepository.saveAndFlush(existing);

        // Overlapping window: starts 15 min into the existing appointment.
        Instant overlapStart = start.plus(15, ChronoUnit.MINUTES);
        Instant overlapEnd = overlapStart.plus(30, ChronoUnit.MINUTES);

        List<Appointment> conflicts = appointmentRepository.findOverlapping(tenant.getId(), UUID.randomUUID(),
                professionalId, null, overlapStart, overlapEnd,
                List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED));

        assertThat(conflicts).containsExactly(existing);
    }

    @Test
    void findOverlappingIgnoresNonOverlappingTimes() {
        Tenant tenant = tenantRepository
                .saveAndFlush(Tenant.create("Tenant Conflict B", "conflict-b", "America/Sao_Paulo"));
        Customer customer = customerRepository
                .saveAndFlush(Customer.create(tenant.getId(), "Cliente", null, null, null, null));
        Service service = serviceRepository
                .saveAndFlush(Service.create(tenant.getId(), "Servico", null, 30, new BigDecimal("50.00")));
        UUID professionalId = UUID.randomUUID();

        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(30, ChronoUnit.MINUTES);
        appointmentRepository.saveAndFlush(
                Appointment.create(tenant.getId(), customer.getId(), service.getId(), professionalId, null, start, end,
                        null));

        // Starts exactly when the existing one ends - back-to-back, not overlapping.
        List<Appointment> conflicts = appointmentRepository.findOverlapping(tenant.getId(), UUID.randomUUID(),
                professionalId, null, end, end.plus(30, ChronoUnit.MINUTES),
                List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED));

        assertThat(conflicts).isEmpty();
    }

    @Test
    void findOverlappingIgnoresDifferentProfessional() {
        Tenant tenant = tenantRepository
                .saveAndFlush(Tenant.create("Tenant Conflict C", "conflict-c", "America/Sao_Paulo"));
        Customer customer = customerRepository
                .saveAndFlush(Customer.create(tenant.getId(), "Cliente", null, null, null, null));
        Service service = serviceRepository
                .saveAndFlush(Service.create(tenant.getId(), "Servico", null, 30, new BigDecimal("50.00")));

        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(30, ChronoUnit.MINUTES);
        appointmentRepository.saveAndFlush(
                Appointment.create(tenant.getId(), customer.getId(), service.getId(), UUID.randomUUID(), null, start,
                        end, null));

        List<Appointment> conflicts = appointmentRepository.findOverlapping(tenant.getId(), UUID.randomUUID(),
                UUID.randomUUID(), null, start, end, List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED));

        assertThat(conflicts).isEmpty();
    }

    @Test
    void findOverlappingIgnoresCancelledAppointments() {
        Tenant tenant = tenantRepository
                .saveAndFlush(Tenant.create("Tenant Conflict D", "conflict-d", "America/Sao_Paulo"));
        Customer customer = customerRepository
                .saveAndFlush(Customer.create(tenant.getId(), "Cliente", null, null, null, null));
        Service service = serviceRepository
                .saveAndFlush(Service.create(tenant.getId(), "Servico", null, 30, new BigDecimal("50.00")));
        UUID professionalId = UUID.randomUUID();

        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(30, ChronoUnit.MINUTES);
        Appointment cancelled = Appointment.create(tenant.getId(), customer.getId(), service.getId(), professionalId,
                null, start, end, null);
        cancelled.cancel();
        appointmentRepository.saveAndFlush(cancelled);

        List<Appointment> conflicts = appointmentRepository.findOverlapping(tenant.getId(), UUID.randomUUID(),
                professionalId, null, start, end, List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED));

        assertThat(conflicts).isEmpty();
    }
}