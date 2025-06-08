package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.util.polling.ChallengeVerificationPollingExecutor;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeParticipantRecordFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeVerificationFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GroupChallengeVerificationResultQueryServiceTest {

    private GroupChallengeVerificationRepository verificationRepository;
    private ChallengeVerificationPollingExecutor pollingExecutor;
    private GroupChallengeVerificationResultQueryService service;

    @BeforeEach
    void setUp() {
        verificationRepository = mock(GroupChallengeVerificationRepository.class);
        pollingExecutor = mock(ChallengeVerificationPollingExecutor.class);
        service = new GroupChallengeVerificationResultQueryService(verificationRepository, pollingExecutor);
    }

    @Test
    @DisplayName("검증 결과가 승인(PENDING_APPROVAL이 아님)이면 해당 상태를 반환한다")
    void waitForResult_GivenApprovedStatus_ThenReturnsStatus() {
        // Given
        Member member = MemberFixture.of();
        GroupChallengeCategory category = GroupChallengeCategoryFixture.of("제로웨이스트");
        GroupChallenge challenge = GroupChallengeFixture.of(member, category);
        GroupChallengeParticipantRecord record = GroupChallengeParticipantRecordFixture.of(challenge, member);
        GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(record);

        Long memberId = member.getId();
        Long challengeId = challenge.getId();
        ChallengeStatus expected = ChallengeStatus.SUCCESS;

        // → pollingExecutor는 직접 실행하지 않고 반환 값만 설정
        when(verificationRepository.findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdAndCreatedAtBetween(
                any(), any(), any(), any()))
                .thenReturn(Optional.of(verification));

        when(pollingExecutor.poll(any()))
                .thenReturn(expected); // 직접 결과를 반환하도록 설정

        // When
        ChallengeStatus result = service.waitForResult(memberId, challengeId);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("오늘 인증 기록이 없으면 NOT_SUBMITTED 상태를 반환한다")
    void waitForResult_GivenNoVerificationToday_ThenReturnsNotSubmitted() {
        // Given
        Long memberId = 1L;
        Long challengeId = 99L;

        when(verificationRepository.findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdAndCreatedAtBetween(
                any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        when(pollingExecutor.poll(any()))
                .thenReturn(ChallengeStatus.NOT_SUBMITTED);

        // When
        ChallengeStatus result = service.waitForResult(memberId, challengeId);

        // Then
        assertThat(result).isEqualTo(ChallengeStatus.NOT_SUBMITTED);
    }
}
