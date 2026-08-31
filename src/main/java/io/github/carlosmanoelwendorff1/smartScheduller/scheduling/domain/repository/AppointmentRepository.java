package io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model.Appointment;
import io.github.carlosmanoelwendorff1.smartScheduller.scheduling.domain.model.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    Optional<Appointment> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<Appointment> findAllByTenantId(UUID tenantId, Pageable pageable);

    /**
     * Finds active appointments (status in activeStatuses) that overlap
     * [startAt, endAt) and share either the same professionalId or the same
     * resourceId - whichever of the two is non-null. excludeId lets a
     * reschedule check against everything except itself; for a brand-new
     * appointment, pass its own freshly-generated id (it can't match
     * anything yet, so the exclusion is a harmless no-op).
     */
    @Query("""
            SELECT a FROM Appointment a
            WHERE a.tenantId = :tenantId
              AND a.id <> :excludeId
              AND a.status IN :activeStatuses
              AND a.startAt < :endAt
              AND a.endAt > :startAt
              AND (
                   (:professionalId IS NOT NULL AND a.professionalId = :professionalId)
                OR (:resourceId IS NOT NULL AND a.resourceId = :resourceId)
              )
            """)
    List<Appointment> findOverlapping(@Param("tenantId") UUID tenantId,
            @Param("excludeId") UUID excludeId,
            @Param("professionalId") UUID professionalId,
            @Param("resourceId") UUID resourceId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt,
            @Param("activeStatuses") List<AppointmentStatus> activeStatuses);

}