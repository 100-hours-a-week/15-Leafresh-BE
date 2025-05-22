package ktb.leafresh.backend.domain.chatbot.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatbotBaseInfoRequestDto(
//        @NotNull(message = "sessionId는 필수입니다.") String sessionId,
        @NotBlank(message = "location은 필수입니다.") String location,
        @NotBlank(message = "workType은 필수입니다.") String workType,
        @NotBlank(message = "category는 필수입니다.") String category
) {}
