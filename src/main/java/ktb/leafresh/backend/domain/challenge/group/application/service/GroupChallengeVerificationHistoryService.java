package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.domain.service.GroupChallengeVerificationHistoryCalculator;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.*;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeVerificationHistoryResponseDto;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupChallengeVerificationHistoryService {

  private final GroupChallengeRepository groupChallengeRepository;
  private final GroupChallengeParticipantRecordRepository groupChallengeParticipantRecordRepository;
  private final GroupChallengeVerificationQueryRepository groupChallengeVerificationQueryRepository;
  private final GroupChallengeVerificationHistoryCalculator
      groupChallengeVerificationHistoryCalculator;

  public GroupChallengeVerificationHistoryResponseDto getVerificationHistory(
      Long memberId, Long challengeId) {
    try {
      GroupChallenge challenge =
          groupChallengeRepository
              .findById(challengeId)
              .orElseThrow(
                  () ->
                      new CustomException(
                          ChallengeErrorCode.GROUP_CHALLENGE_VERIFICATION_CHALLENGE_NOT_FOUND));

      GroupChallengeParticipantRecord record =
          groupChallengeParticipantRecordRepository
              .findByMemberIdAndGroupChallengeIdAndDeletedAtIsNull(memberId, challengeId)
              .orElseThrow(
                  () ->
                      new CustomException(
                          ChallengeErrorCode.GROUP_CHALLENGE_VERIFICATION_ACCESS_DENIED));

      List<GroupChallengeVerification> verifications =
          groupChallengeVerificationQueryRepository.findByParticipantRecordId(record.getId());

      return groupChallengeVerificationHistoryCalculator.calculate(
          challenge, record, verifications);
    } catch (CustomException e) {
      throw e;
    } catch (Exception e) {
      throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_VERIFICATION_READ_FAILED);
    }
  }
}
