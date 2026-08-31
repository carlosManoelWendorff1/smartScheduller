/**
 * Availability configuration module: BusinessHours (tenant-wide default) and
 * ProfessionalAvailabilityRule (per-professional override, falls back to
 * BusinessHours when absent - see
 * ProfessionalAvailabilityRuleService.findEffective).
 * The future AvailabilityEngine (slot calculation) will live here too.
 */
package io.github.carlosmanoelwendorff1.smartScheduller.availability;