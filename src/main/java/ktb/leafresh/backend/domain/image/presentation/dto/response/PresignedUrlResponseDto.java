package ktb.leafresh.backend.domain.image.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PresignedUrlResponseDto(
    @Schema(
            description = "PUT 요청을 보낼 presigned URL",
            example = "https://storage.googleapis.com/...")
        String uploadUrl,
    @Schema(description = "업로드된 이미지가 접근 가능한 URL", example = "https://storage.googleapis.com/...")
        String fileUrl) {}
