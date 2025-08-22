package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.member.application.service.BadgeGrantManager;
import ktb.leafresh.backend.domain.member.application.service.RewardGrantService;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.notification.application.service.NotificationCreateService;
import ktb.leafresh.backend.domain.notification.domain.entity.enums.NotificationType;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.VerificationResultRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeType;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import ktb.leafresh.backend.support.fixture.GroupChallengeVerificationFixture;
import ktb.leafresh.backend.support.fixture.PersonalChallengeVerificationFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("VerificationResultProcessor 테스트")
@ExtendWith(MockitoExtension.class)
class VerificationResultProcessorTest {

  @Mock private GroupChallengeVerificationRepository groupChallengeVerificationRepository;

  @Mock private PersonalChallengeVerificationRepository personalChallengeVerificationRepository;

  @Mock private NotificationCreateService notificationCreateService;

  @Mock private RewardGrantService rewardGrantService;

  @Mock private BadgeGrantManager badgeGrantManager;

  @Mock private GroupChallengeParticipantRecord participantRecord;

  @Mock private GroupChallenge groupChallenge;

  @Mock private Member member;

  @Mock private PersonalChallenge personalChallenge;

  @InjectMocks private VerificationResultProcessor verificationResultProcessor;

  @Test
  @DisplayName("단체 인증 결과 처리 - 성공 케이스")
  void processGroup_withSuccessResult_updatesStatusAndGrantsReward() {
    // given
    Long verificationId = 1L;
    GroupChallengeVerification verification =
        GroupChallengeVerificationFixture.of(participantRecord, ChallengeStatus.PENDING_APPROVAL);
    ReflectionTestUtils.setField(verification, "rewarded", false);

    given(groupChallengeVerificationRepository.findById(verificationId))
        .willReturn(Optional.of(verification));
    given(participantRecord.getMember()).willReturn(member);
    given(participantRecord.getGroupChallenge()).willReturn(groupChallenge);
    given(groupChallenge.getTitle()).willReturn("제로웨이스트 5일 챌린지");
    given(groupChallenge.getId()).willReturn(123L);
    given(groupChallenge.getLeafReward()).willReturn(10);
    given(participantRecord.getVerifications()).willReturn(List.of(verification));
    given(participantRecord.isAllSuccess()).willReturn(true);
    given(groupChallenge.getDurationInDays()).willReturn(1);
    given(participantRecord.hasReceivedParticipationBonus()).willReturn(false);

    VerificationResultRequestDto dto =
        VerificationResultRequestDto.builder()
            .type(ChallengeType.GROUP)
            .memberId(1L)
            .challengeId(123L)
            .verificationId(verificationId)
            .date("2024-01-01")
            .result("true")
            .build();

    // when
    verificationResultProcessor.process(verificationId, dto);

    // then
    assertThat(verification.getStatus()).isEqualTo(ChallengeStatus.SUCCESS);
    verify(notificationCreateService)
        .createChallengeVerificationResultNotification(
            member,
            "제로웨이스트 5일 챌린지",
            true,
            NotificationType.GROUP,
            verification.getImageUrl(),
            123L);
    verify(rewardGrantService).grantLeafPoints(member, 10);
    verify(rewardGrantService).grantParticipationBonus(member, participantRecord);
    verify(badgeGrantManager).evaluateAllAndGrant(member);
  }

  @Test
  @DisplayName("개인 인증 결과 처리 - 실패 케이스")
  void processPersonal_withFailResult_updatesStatusAndSkipsReward() {
    // given
    Long verificationId = 2L;
    PersonalChallengeVerification verification =
        PersonalChallengeVerificationFixture.of(member, personalChallenge);
    ReflectionTestUtils.setField(verification, "status", ChallengeStatus.PENDING_APPROVAL);
    ReflectionTestUtils.setField(verification, "rewarded", false);

    given(personalChallengeVerificationRepository.findById(verificationId))
        .willReturn(Optional.of(verification));
    given(personalChallenge.getTitle()).willReturn("텀블러 사용 챌린지");
    given(personalChallenge.getId()).willReturn(321L);

    VerificationResultRequestDto dto =
        VerificationResultRequestDto.builder()
            .type(ChallengeType.PERSONAL)
            .memberId(1L)
            .challengeId(321L)
            .verificationId(verificationId)
            .date("2025-07-01")
            .result("false")
            .build();

    // when
    verificationResultProcessor.process(verificationId, dto);

    // then
    assertThat(verification.getStatus()).isEqualTo(ChallengeStatus.FAILURE);
    verify(notificationCreateService)
        .createChallengeVerificationResultNotification(
            member,
            "텀블러 사용 챌린지",
            false,
            NotificationType.PERSONAL,
            verification.getImageUrl(),
            321L);
    verify(rewardGrantService, never()).grantLeafPoints(any(), anyInt());
    verify(badgeGrantManager).evaluateAllAndGrant(member);
  }

  @Test
  @DisplayName("단체 인증 결과 처리 - 인증 ID 없음 → 예외 발생")
  void processGroup_withInvalidVerificationId_throwsException() {
    // given
    Long invalidId = 999L;
    given(groupChallengeVerificationRepository.findById(invalidId)).willReturn(Optional.empty());

    VerificationResultRequestDto dto =
        VerificationResultRequestDto.builder()
            .type(ChallengeType.GROUP)
            .memberId(1L)
            .challengeId(123L)
            .verificationId(invalidId)
            .date("2025-07-03")
            .result("true")
            .build();

    // when & then
    assertThatThrownBy(() -> verificationResultProcessor.process(invalidId, dto))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(VerificationErrorCode.VERIFICATION_NOT_FOUND.getMessage());
  }

  @Test
  @DisplayName("result 값이 true/false가 아닌 경우 인증 무시")
  void process_withInvalidResultValue_doesNothing() {
    // given
    VerificationResultRequestDto dto =
        VerificationResultRequestDto.builder()
            .type(ChallengeType.GROUP)
            .memberId(1L)
            .challengeId(123L)
            .verificationId(1L)
            .date("2025-07-03")
            .result("not_a_boolean")
            .build();

    // when
    verificationResultProcessor.process(1L, dto);

    // then
    verifyNoInteractions(groupChallengeVerificationRepository);
    verifyNoInteractions(notificationCreateService);
    verifyNoInteractions(rewardGrantService);
    verifyNoInteractions(badgeGrantManager);
  }
}
