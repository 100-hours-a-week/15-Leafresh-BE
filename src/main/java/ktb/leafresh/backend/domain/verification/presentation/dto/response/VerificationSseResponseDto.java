package ktb.leafresh.backend.domain.verification.presentation.dto.response;

import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;

public record VerificationSseResponseDto(String status, String message) {

    public static VerificationSseResponseDto fromStatus(ChallengeStatus status) {
        return new VerificationSseResponseDto(status.name(), resolveMessage(status));
    }

    public static VerificationSseResponseDto fromBoolean(boolean result) {
        return fromStatus(result ? ChallengeStatus.SUCCESS : ChallengeStatus.FAILURE);
    }

    public static String resolveMessage(ChallengeStatus status) {
        return switch (status) {
            case SUCCESS -> "인증에 성공했습니다.";
            case FAILURE -> "인증에 실패했습니다.";
            case PENDING_APPROVAL -> "아직 인증 결과가 도착하지 않았습니다.";
            case NOT_SUBMITTED -> "아직 인증을 제출하지 않았습니다.";
        };
    }
}
