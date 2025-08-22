package ktb.leafresh.backend.domain.store.product.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "타임딜 생성 응답")
public record TimedealCreateResponseDto(
    @Schema(description = "생성된 타임딜의 ID", example = "1") Long dealId) {}
