package ktb.leafresh.backend.domain.verification.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeType;
import lombok.Builder;

@Schema(description = "인증 결과 요청 DTO")
@Builder
public record VerificationResultRequestDto(
    @NotNull(message = "type은 필수입니다.") @Schema(description = "챌린지 타입", example = "GROUP")
        ChallengeType type, // PERSONAL 또는 GROUP
    @NotNull(message = "memberId는 필수입니다.") @Schema(description = "회원 ID", example = "1")
        Long memberId,
    @NotNull(message = "challengeId는 필수입니다.") @Schema(description = "챌린지 ID", example = "10")
        Long challengeId,
    @NotNull(message = "verificationId는 필수입니다.") @Schema(description = "인증 ID", example = "100")
        Long verificationId,
    @NotNull(message = "date는 필수입니다.") @Schema(description = "인증 날짜", example = "2024-12-20")
        String date,
    @NotNull(message = "result는 필수입니다.")
        @Schema(description = "인증 결과 (true/false 또는 HTTP 상태 코드)", example = "true")
        String result) {

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
