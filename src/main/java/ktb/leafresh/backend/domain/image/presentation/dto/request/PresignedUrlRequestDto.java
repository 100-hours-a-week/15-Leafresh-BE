package ktb.leafresh.backend.domain.image.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public record PresignedUrlRequestDto(
    @NotBlank @Schema(description = "업로드할 파일 이름", example = "post-image-123.png") String fileName,
    @NotBlank @Schema(description = "Content-Type", example = "image/png") String contentType) {}
