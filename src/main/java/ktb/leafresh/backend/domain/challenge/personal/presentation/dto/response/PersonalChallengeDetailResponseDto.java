package ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import lombok.Builder;

import java.time.LocalTime;
import java.util.List;

@Schema(description = "개인 챌린지 상세 정보 응답")
@Builder
public record PersonalChallengeDetailResponseDto(
    @Schema(description = "챌린지 ID", example = "1") Long id,
    @Schema(description = "챌린지 제목", example = "매일 물 8잔 마시기") String title,
    @Schema(description = "챌린지 설명", example = "건강한 하루를 위해 물 8잔을 마셔보세요!") String description,
    @Schema(description = "썸네일 이미지 URL", example = "https://leafresh.io/thumbnail.jpg")
        String thumbnailUrl,
    @Schema(description = "챌린지 실행 요일", example = "MONDAY") DayOfWeek dayOfWeek,
    @Schema(description = "인증 시작 시간", example = "09:00") LocalTime verificationStartTime,
    @Schema(description = "인증 종료 시간", example = "21:00") LocalTime verificationEndTime,
    @Schema(description = "리프 보상 포인트", example = "10") Integer leafReward,
    @Schema(description = "예시 이미지 목록") List<PersonalChallengeExampleImageDto> exampleImages,
    @Schema(description = "챌린지 상태", example = "ACTIVE") ChallengeStatus status) {

  public static PersonalChallengeDetailResponseDto of(
      PersonalChallenge challenge,
      List<PersonalChallengeExampleImageDto> images,
      ChallengeStatus status) {
    return PersonalChallengeDetailResponseDto.builder()
        .id(challenge.getId())
        .title(challenge.getTitle())
        .description(challenge.getDescription())
        .thumbnailUrl(challenge.getImageUrl())
        .dayOfWeek(challenge.getDayOfWeek())
        .verificationStartTime(challenge.getVerificationStartTime())
        .verificationEndTime(challenge.getVerificationEndTime())
        .leafReward(challenge.getLeafReward())
        .exampleImages(images)
        .status(status)
        .build();
  }
}
