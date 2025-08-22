package ktb.leafresh.backend.domain.feedback.infrastructure.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Schema(
    name = "AiFeedbackCreationRequest",
    description = "AI 피드백 생성을 위한 회원 활동 데이터 DTO (외부 AI 서비스 요청용)")
public record AiFeedbackCreationRequestDto(
    @Schema(
            description = "피드백 대상 회원 ID",
            example = "12345",
            requiredMode = Schema.RequiredMode.REQUIRED)
        Long memberId,
    @Schema(description = "회원의 개인 챌린지 활동 내역", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        List<PersonalChallengeDto> personalChallenges,
    @Schema(description = "회원의 그룹 챌린지 활동 내역", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        List<GroupChallengeDto> groupChallenges) {

  @Builder
  @Schema(name = "PersonalChallenge", description = "개인 챌린지 정보")
  public record PersonalChallengeDto(
      @Schema(
              description = "개인 챌린지 ID",
              example = "1001",
              requiredMode = Schema.RequiredMode.REQUIRED)
          Long id,
      @Schema(
              description = "개인 챌린지 제목",
              example = "하루 30분 걷기",
              requiredMode = Schema.RequiredMode.REQUIRED)
          String title,
      @Schema(
              description = "챌린지 성공 여부",
              example = "true",
              requiredMode = Schema.RequiredMode.REQUIRED)
          boolean isSuccess) {}

  @Builder
  @Schema(name = "GroupChallenge", description = "그룹 챌린지 정보")
  public record GroupChallengeDto(
      @Schema(
              description = "그룹 챌린지 ID",
              example = "2001",
              requiredMode = Schema.RequiredMode.REQUIRED)
          Long id,
      @Schema(
              description = "그룹 챌린지 제목",
              example = "친환경 제품 사용하기",
              requiredMode = Schema.RequiredMode.REQUIRED)
          String title,
      @Schema(
              description = "챌린지 시작 일시",
              example = "2024-01-15T09:00:00",
              requiredMode = Schema.RequiredMode.REQUIRED)
          LocalDateTime startDate,
      @Schema(
              description = "챌린지 종료 일시",
              example = "2024-01-21T23:59:59",
              requiredMode = Schema.RequiredMode.REQUIRED)
          LocalDateTime endDate,
      @Schema(description = "회원의 그룹 챌린지 참여 기록 목록", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
          List<SubmissionDto> submissions) {
    public GroupChallengeDto {
      submissions = submissions != null ? submissions : List.of();
    }

    @Builder
    @Schema(name = "Submission", description = "그룹 챌린지 참여 기록")
    public record SubmissionDto(
        @Schema(
                description = "참여 성공 여부",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
            boolean isSuccess,
        @Schema(
                description = "참여 제출 일시",
                example = "2024-01-16T14:30:00",
                requiredMode = Schema.RequiredMode.REQUIRED)
            LocalDateTime submittedAt) {}
  }
}
