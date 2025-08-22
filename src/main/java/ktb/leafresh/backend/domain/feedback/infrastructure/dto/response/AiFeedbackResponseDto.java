package ktb.leafresh.backend.domain.feedback.infrastructure.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AiFeedbackResponse", description = "외부 AI 서비스로부터 받은 피드백 응답 데이터")
public record AiFeedbackResponseDto(
    @Schema(
            description = "AI가 생성한 피드백 내용",
            example =
                "지난주 개인 챌린지를 3개 모두 성공하셨네요! 꾸준한 노력이 돋보입니다. 이번 주에는 그룹 챌린지에도 적극적으로 참여해보시는 것을 추천드려요.",
            requiredMode = Schema.RequiredMode.REQUIRED)
        String content) {}
