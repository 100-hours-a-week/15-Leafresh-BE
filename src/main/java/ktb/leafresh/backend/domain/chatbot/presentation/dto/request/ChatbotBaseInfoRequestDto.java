package ktb.leafresh.backend.domain.chatbot.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "챗봇 기본 정보 요청 DTO")
public record ChatbotBaseInfoRequestDto(
    @Schema(
            description = "세션 ID",
            example = "session123",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "sessionId는 필수입니다.")
        String sessionId,
    @Schema(description = "지역 정보", example = "서울", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "location은 필수입니다.")
        String location,
    @Schema(description = "근무 형태", example = "사무직", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "workType은 필수입니다.")
        String workType,
    @Schema(description = "카테고리", example = "환경", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "category는 필수입니다.")
        String category) {}
