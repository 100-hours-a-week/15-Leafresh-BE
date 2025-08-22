package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.*;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.*;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupChallengeDetailReadService {

  private final GroupChallengeRepository groupChallengeRepository;
  private final GroupChallengeVerificationRepository verificationRepository;
  private final GroupChallengeParticipantRecordRepository participantRecordRepository;
  private final Clock clock;

  public GroupChallengeDetailResponseDto getChallengeDetail(Long memberIdOrNull, Long challengeId) {
    try {
      GroupChallenge challenge = getChallengeOrThrow(challengeId);

      if (challenge.isDeleted()) {
        throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_ALREADY_DELETED);
      }

      List<String> verificationImages = getVerificationImages(challengeId);
      List<GroupChallengeExampleImageDto> exampleImages = getExampleImages(challenge);
      ChallengeStatus status = resolveChallengeStatus(memberIdOrNull, challengeId);

      return GroupChallengeDetailResponseDto.of(
          challenge, exampleImages, verificationImages, status);
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      log.error("단체 챌린지 상세 조회 실패", e);
      throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_DETAIL_READ_FAILED);
    }
  }

  private GroupChallenge getChallengeOrThrow(Long challengeId) {
    return groupChallengeRepository
        .findById(challengeId)
        .orElseThrow(() -> new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_NOT_FOUND));
  }

  private List<String> getVerificationImages(Long challengeId) {
    return verificationRepository
        .findTop9ByParticipantRecord_GroupChallenge_IdOrderByCreatedAtDesc(challengeId)
        .stream()
        .map(GroupChallengeVerification::getImageUrl)
        .toList();
  }

  private List<GroupChallengeExampleImageDto> getExampleImages(GroupChallenge challenge) {
    return challenge.getExampleImages().stream().map(GroupChallengeExampleImageDto::from).toList();
  }

  private ChallengeStatus resolveChallengeStatus(Long memberIdOrNull, Long challengeId) {
    if (memberIdOrNull == null) {
      log.info("비로그인 상태 - 인증 상태 조회 생략");
      return ChallengeStatus.NOT_SUBMITTED;
    }

    GroupChallenge challenge = getChallengeOrThrow(challengeId);

    // 참가 여부 확인
    boolean hasParticipated =
        participantRecordRepository.existsByMember_IdAndGroupChallenge_Id(
            memberIdOrNull, challengeId);

    if (!hasParticipated) {
      return ChallengeStatus.NOT_PARTICIPATED;
    }

    // 날짜 계산
    ZoneId kst = ZoneId.of("Asia/Seoul");
    LocalDate todayInKST = LocalDate.now(clock.withZone(kst));
    LocalDateTime startOfToday = todayInKST.atStartOfDay();
    LocalDateTime endOfToday = todayInKST.atTime(23, 59, 59);
    ZonedDateTime startUtc = startOfToday.atZone(kst).withZoneSameInstant(ZoneId.of("UTC"));
    ZonedDateTime endUtc = endOfToday.atZone(kst).withZoneSameInstant(ZoneId.of("UTC"));

    LocalDateTime now = LocalDateTime.now(clock);

    // 인증 기간 내일 때 오늘 인증 여부 확인
    if (!now.isBefore(challenge.getStartDate()) && !now.isAfter(challenge.getEndDate())) {
      return verificationRepository
          .findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdAndCreatedAtBetween(
              memberIdOrNull, challengeId, startUtc.toLocalDateTime(), endUtc.toLocalDateTime())
          .map(GroupChallengeVerification::getStatus)
          .orElse(ChallengeStatus.NOT_SUBMITTED);
    }

    // 인증 기간 외일 경우 마지막 인증 상태 반환
    return verificationRepository
        .findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdOrderByCreatedAtDesc(
            memberIdOrNull, challengeId)
        .map(GroupChallengeVerification::getStatus)
        .orElse(ChallengeStatus.NOT_SUBMITTED);
  }
}
