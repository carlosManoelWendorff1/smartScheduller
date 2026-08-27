package io.github.carlosmanoelwendorff1.smartScheduller.scheduling.infrastructure;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import io.github.carlosmanoelwendorff1.smartScheduller.AbstractIntegrationTest;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.model.service.Service;
import io.github.carlosmanoelwendorff1.smartScheduller.catalog.domain.repository.service.ServiceRepository;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.model.Customer;
import io.github.carlosmanoelwendorff1.smartScheduller.customer.domain.repository.CustomerRepository;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model.Appointment;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.repository.AppointmentRepository;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.model.Tenant;
import io.github.carlosmanoelwendorff1.smartScheduller.tenant.domain.repository.TenantRepository;

class AppointmentRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    private Tenant tenant;
    private Customer customer;
    private Service service;

    private void setUpFixtures(String slug) {
        tenant = tenantRepository.saveAndFlush(Tenant.create("Tenant " + slug, slug, "America/Sao_Paulo"));
        customer = customerRepository.saveAndFlush(Customer.create(tenant.getId(), "Cliente", null, null, null, null));
        service = serviceRepository.saveAndFlush(
                Service.create(tenant.getId(), "Servico", null, 30, new BigDecimal("50.00")));
    }

    private Appointment newAppointment() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        return Appointment.create(tenant.getId(), customer.getId(), service.getId(), null, null, start,
                start.plusSeconds(1800), null);
    }

    @Test
    void persistsAndReloadsAnAppointment() {
        setUpFixtures("appt-repo-a");
        Appointment appointment = newAppointment();

        appointmentRepository.saveAndFlush(appointment);

        Appointment reloaded = appointmentRepository.findByIdAndTenantId(appointment.getId(), tenant.getId())
                .orElseThrow();
        assertThat(reloaded.getCustomerId()).isEqualTo(customer.getId());
        assertThat(reloaded.getServiceId()).isEqualTo(service.getId());
    }

    @Test
    void doesNotReturnAppointmentForAnotherTenant() {
        setUpFixtures("appt-repo-b1");
        Tenant otherTenant = tenantRepository.saveAndFlush(
                Tenant.create("Outro", "appt-repo-b2", "America/Sao_Paulo"));
        Appointment appointment = newAppointment();
        appointmentRepository.saveAndFlush(appointment);

        assertThat(appointmentRepository.findByIdAndTenantId(appointment.getId(), otherTenant.getId())).isEmpty();
    }

    @Test
    void databaseRejectsAppointmentForUnknownCustomer() {
        setUpFixtures("appt-repo-c");
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Appointment appointment = Appointment.create(tenant.getId(), UUID.randomUUID(), service.getId(), null, null,
                start, start.plusSeconds(1800), null);

        assertThatThrownBy(() -> appointmentRepository.saveAndFlush(appointment))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsAppointmentForUnknownService() {
        setUpFixtures("appt-repo-d");
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Appointment appointment = Appointment.create(tenant.getId(), customer.getId(), UUID.randomUUID(), null, null,
                start, start.plusSeconds(1800), null);

        assertThatThrownBy(() -> appointmentRepository.saveAndFlush(appointment))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}