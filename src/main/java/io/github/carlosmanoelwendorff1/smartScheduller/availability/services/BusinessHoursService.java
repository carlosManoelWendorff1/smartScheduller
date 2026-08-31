package io.github.carlosmanoelwendorff1.smartScheduller.availability.services;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.model.BusinessHours;
import io.github.carlosmanoelwendorff1.smartScheduller.availability.domain.repository.BusinessHoursRepository;

@Service
@Transactional
public class BusinessHoursService {

    private final BusinessHoursRepository businessHoursRepository;

    public BusinessHoursService(BusinessHoursRepository businessHoursRepository) {
        this.businessHoursRepository = businessHoursRepository;
    }

    /**
     * Create-or-update: one row per (tenant, dayOfWeek) - the day itself is the
     * natural key.
     */
    public BusinessHours upsert(UUID tenantId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
            boolean closed) {
        return businessHoursRepository.findByTenantIdAndDayOfWeek(tenantId, dayOfWeek)
                .map(existing -> {
                    existing.update(startTime, endTime, closed);
                    return existing;
                })
                .orElseGet(() -> businessHoursRepository.save(
                        BusinessHours.create(tenantId, dayOfWeek, startTime, endTime, closed)));
    }

    @Transactional(readOnly = true)
    public List<BusinessHours> findAll(UUID tenantId) {
        return businessHoursRepository.findAllByTenantId(tenantId);
    }

    public void delete(UUID tenantId, DayOfWeek dayOfWeek) {
        businessHoursRepository.findByTenantIdAndDayOfWeek(tenantId, dayOfWeek)
                .ifPresent(businessHoursRepository::delete);
    }
}