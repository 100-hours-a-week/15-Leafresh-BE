package ktb.leafresh.backend.domain.verification.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeType;
import lombok.Builder;

@Builder
public record VerificationResultRequestDto(
        @NotNull(message = "type은 필수입니다.")
        ChallengeType type, // PERSONAL 또는 GROUP

        @NotNull(message = "memberId는 필수입니다.")
        Long memberId,

        @NotNull(message = "challengeId는 필수입니다.")
        Long challengeId,

        @NotNull(message = "verificationId는 필수입니다.")
        Long verificationId,

        @NotNull(message = "date는 필수입니다.")
        String date,

        @NotNull(message = "result는 필수입니다.")
        String result
) {
        public boolean isSuccessResult() {
                return "true".equalsIgnoreCase(result) || "false".equalsIgnoreCase(result);
        }

        public boolean resultAsBoolean() {
                return Boolean.parseBoolean(result);
        }

        public boolean isRecoverableHttpError() {
                return result.matches("4\\d\\d|5\\d\\d");
        }

        public int resultAsHttpStatus() {
                return Integer.parseInt(result);
        }
}
