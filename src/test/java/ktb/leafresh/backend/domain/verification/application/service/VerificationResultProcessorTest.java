package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

class VerificationResultProcessorTest {

    private GroupChallengeVerificationRepository groupRepo;
    private PersonalChallengeVerificationRepository personalRepo;
    private NotificationCreateService notificationService;
    private RewardGrantService rewardGrantService;
    private BadgeGrantManager badgeGrantManager;
    private VerificationResultProcessor processor;

    @BeforeEach
    void setUp() {
        groupRepo = mock(GroupChallengeVerificationRepository.class);
        personalRepo = mock(PersonalChallengeVerificationRepository.class);
        notificationService = mock(NotificationCreateService.class);
        rewardGrantService = mock(RewardGrantService.class);
        badgeGrantManager = mock(BadgeGrantManager.class);
        processor = new VerificationResultProcessor(groupRepo, personalRepo, notificationService, rewardGrantService, badgeGrantManager);
    }

    @Test
    @DisplayName("단체 인증 성공 → 상태 업데이트 + 알림 + 보상 + 뱃지 지급")
    void processGroupSuccess_ShouldGrantRewardAndBadge() {
        // Given
        Long verificationId = 1L;
        VerificationResultRequestDto dto = VerificationResultRequestDto.builder()
                .type(ChallengeType.GROUP)
                .memberId(1L)
                .challengeId(1L)
                .date("2025-05-28")
                .result(true)
                .build();

        Member member = mock(Member.class);
        when(member.getId()).thenReturn(1L);

        GroupChallenge challenge = mock(GroupChallenge.class);
        when(challenge.getTitle()).thenReturn("제로 챌린지");
        when(challenge.getLeafReward()).thenReturn(10);
        when(challenge.getDurationInDays()).thenReturn(3);

        GroupChallengeVerification verification = mock(GroupChallengeVerification.class);
        GroupChallengeParticipantRecord record = mock(GroupChallengeParticipantRecord.class);

        when(groupRepo.findById(verificationId)).thenReturn(Optional.of(verification));
        when(verification.getParticipantRecord()).thenReturn(record);
        when(verification.getImageUrl()).thenReturn("url");
        when(verification.isRewarded()).thenReturn(false);
        when(record.getMember()).thenReturn(member);
        when(record.getGroupChallenge()).thenReturn(challenge);
        when(record.isAllSuccess()).thenReturn(false);

        // When
        processor.process(verificationId, dto);

        // Then
        verify(verification).markVerified(ChallengeStatus.SUCCESS);
        verify(rewardGrantService).grantLeafPoints(member, 10);
        verify(verification).markRewarded();
        verify(notificationService).createChallengeVerificationResultNotification(eq(member), eq("제로 챌린지"), eq(true), eq(NotificationType.GROUP), eq("url"), any());
        verify(badgeGrantManager).evaluateAllAndGrant(member);
    }

    @Test
    @DisplayName("개인 인증 실패 → 상태만 업데이트 + 알림")
    void processPersonalFail_ShouldUpdateStatusOnly() {
        // Given
        Long verificationId = 2L;
        VerificationResultRequestDto dto = VerificationResultRequestDto.builder()
                .type(ChallengeType.PERSONAL)
                .memberId(1L)
                .challengeId(1L)
                .date("2025-05-28")
                .result(false)
                .build();

        Member member = mock(Member.class);
        when(member.getId()).thenReturn(2L);

        var challenge = mock(ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge.class);
        when(challenge.getTitle()).thenReturn("텀블러 사용");
        when(challenge.getId()).thenReturn(99L);

        PersonalChallengeVerification verification = mock(PersonalChallengeVerification.class);
        when(verification.getMember()).thenReturn(member);
        when(verification.getImageUrl()).thenReturn("img");
        when(verification.getPersonalChallenge()).thenReturn(challenge);
        when(personalRepo.findById(verificationId)).thenReturn(Optional.of(verification));

        // When
        processor.process(verificationId, dto);

        // Then
        verify(verification).markVerified(ChallengeStatus.FAILURE);
        verify(notificationService).createChallengeVerificationResultNotification(
                eq(member), eq("텀블러 사용"), eq(false), eq(NotificationType.PERSONAL), eq("img"), eq(99L)
        );
        verifyNoInteractions(rewardGrantService);
        verify(badgeGrantManager).evaluateAllAndGrant(member);
    }
}
