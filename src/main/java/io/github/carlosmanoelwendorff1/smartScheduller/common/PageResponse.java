package io.github.carlosmanoelwendorff1.smartScheduller.common;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Generic pagination envelope for list endpoints.
 * <p>
 * We deliberately don't return Spring Data's {@link Page} directly from
 * controllers: it exposes internal/implementation details (e.g. "pageable",
 * "sort" objects) that are not part of a stable public API contract. This
 * type lives in the root package of the "common" module, which makes it
 * automatically part of that module's public API under Spring Modulith's
 * default visibility rules, so any other module can depend on it.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast());
    }
}