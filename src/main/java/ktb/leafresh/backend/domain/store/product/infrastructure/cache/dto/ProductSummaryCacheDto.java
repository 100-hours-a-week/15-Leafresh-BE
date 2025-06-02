package ktb.leafresh.backend.domain.store.product.infrastructure.cache.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ProductSummaryCacheDto(
        Long id,
        String title,
        String description,
        String imageUrl,
        int price,
        int stock,
        String status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {}
