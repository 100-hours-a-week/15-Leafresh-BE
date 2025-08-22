package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Schema(description = "단체 챌린지 상세 정보 응답")
@Builder
public record GroupChallengeDetailResponseDto(
    @Schema(description = "챌린지 ID", example = "1") Long id,
    @Schema(description = "이벤트 챌린지 여부", example = "false") boolean isEvent,
    @Schema(description = "챌린지 제목", example = "제로웨이스트 챌린지") String title,
    @Schema(description = "챌린지 설명", example = "환경을 위한 제로웨이스트 챌린지에 참여해보세요!") String description,
    @Schema(description = "카테고리", example = "ZERO_WASTE") String category,
    @Schema(description = "챌린지 시작일", example = "2024-01-01T00:00:00+00:00")
        OffsetDateTime startDate,
    @Schema(description = "챌린지 종료일", example = "2024-01-31T23:59:59+00:00") OffsetDateTime endDate,
    @Schema(description = "인증 시작 시간", example = "09:00") LocalTime verificationStartTime,
    @Schema(description = "인증 종료 시간", example = "21:00") LocalTime verificationEndTime,
    @Schema(description = "리프 보상 포인트", example = "20") Integer leafReward,
    @Schema(description = "썸네일 이미지 URL", example = "https://leafresh.io/thumbnail.jpg")
        String thumbnailUrl,
    @Schema(description = "예시 이미지 목록") List<GroupChallengeExampleImageDto> exampleImages,
    @Schema(description = "인증 이미지 목록") List<String> verificationImages,
    @Schema(description = "최대 참여 인원", example = "50") int maxParticipantCount,
    @Schema(description = "현재 참여 인원", example = "25") int currentParticipantCount,
    @Schema(description = "챌린지 상태", example = "ACTIVE") ChallengeStatus status) {

  public static GroupChallengeDetailResponseDto of(
      GroupChallenge challenge,
      List<GroupChallengeExampleImageDto> exampleImages,
      List<String> verificationImages,
      ChallengeStatus status) {
    return GroupChallengeDetailResponseDto.builder()
        .id(challenge.getId())
        .isEvent(Boolean.TRUE.equals(challenge.getEventFlag()))
        .title(challenge.getTitle())
        .description(challenge.getDescription())
        .category(challenge.getCategory().getName())
        .startDate(challenge.getStartDate().atOffset(ZoneOffset.UTC))
        .endDate(challenge.getEndDate().atOffset(ZoneOffset.UTC))
        .verificationStartTime(challenge.getVerificationStartTime())
        .verificationEndTime(challenge.getVerificationEndTime())
        .leafReward(challenge.getLeafReward())
        .thumbnailUrl(challenge.getImageUrl())
        .exampleImages(exampleImages)
        .verificationImages(verificationImages)
        .maxParticipantCount(challenge.getMaxParticipantCount())
        .currentParticipantCount(challenge.getCurrentParticipantCount())
        .status(status)
        .build();
  }
}
