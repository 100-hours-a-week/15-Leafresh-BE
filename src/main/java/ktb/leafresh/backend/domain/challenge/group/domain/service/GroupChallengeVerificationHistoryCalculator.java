package ktb.leafresh.backend.domain.challenge.group.domain.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeVerificationHistoryResponseDto;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupChallengeVerificationHistoryCalculator {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");

  public GroupChallengeVerificationHistoryResponseDto calculate(
      GroupChallenge challenge,
      GroupChallengeParticipantRecord record,
      List<GroupChallengeVerification> verifications) {
    // 시작일/종료일을 KST 기준으로 변환
    LocalDate startDateKST =
        challenge.getStartDate().atZone(ZoneOffset.UTC).withZoneSameInstant(KST).toLocalDate();

    LocalDate endDateKST =
        challenge.getEndDate().atZone(ZoneOffset.UTC).withZoneSameInstant(KST).toLocalDate();

    LocalDate todayKST = LocalDate.now(KST);

    // 인증 기록 정렬 및 day 계산 (KST 기준)
    List<GroupChallengeVerificationHistoryResponseDto.VerificationDto> verificationDtos =
        verifications.stream()
            .sorted(Comparator.comparing(GroupChallengeVerification::getCreatedAt).reversed())
            .map(
                v -> {
                  LocalDate createdDateKST =
                      v.getCreatedAt()
                          .atZone(ZoneOffset.UTC)
                          .withZoneSameInstant(KST)
                          .toLocalDate();

                  int day = (int) ChronoUnit.DAYS.between(startDateKST, createdDateKST) + 1;

                  return GroupChallengeVerificationHistoryResponseDto.VerificationDto.builder()
                      .day(day)
                      .imageUrl(v.getImageUrl())
                      .status(v.getStatus())
                      .build();
                })
            .collect(Collectors.toList());

    long success =
        verifications.stream().filter(v -> v.getStatus() == ChallengeStatus.SUCCESS).count();

    long failure =
        verifications.stream().filter(v -> v.getStatus() == ChallengeStatus.FAILURE).count();

    int remaining = (int) Math.max(0, ChronoUnit.DAYS.between(todayKST, endDateKST) + 1);

    // 오늘 인증 여부도 KST 기준으로 판단
    String todayStatus =
        verifications.stream()
            .filter(
                v -> {
                  LocalDate createdDateKST =
                      v.getCreatedAt()
                          .atZone(ZoneOffset.UTC)
                          .withZoneSameInstant(KST)
                          .toLocalDate();
                  return createdDateKST.isEqual(todayKST);
                })
            .findFirst()
            .map(
                v ->
                    switch (v.getStatus()) {
                      case SUCCESS, FAILURE -> "DONE";
                      case PENDING_APPROVAL -> "PENDING_APPROVAL";
                      default -> "NOT_SUBMITTED";
                    })
            .orElse("NOT_SUBMITTED");

    return GroupChallengeVerificationHistoryResponseDto.builder()
        .id(challenge.getId())
        .title(challenge.getTitle())
        .achievement(
            new GroupChallengeVerificationHistoryResponseDto.AchievementDto(
                (int) success, (int) failure, remaining))
        .verifications(verificationDtos)
        .todayStatus(todayStatus)
        .build();
  }
}
