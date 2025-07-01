package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
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
    private GroupChallengeVerificationResultQueryService service;

    @BeforeEach
    void setUp() {
        verificationRepository = mock(GroupChallengeVerificationRepository.class);
        service = new GroupChallengeVerificationResultQueryService(verificationRepository);
    }

    @Test
    @DisplayName("오늘 인증 상태가 존재하면 해당 상태를 반환한다")
    void getLatestStatus_GivenTodayVerificationExists_ThenReturnsStatus() {
        // Given
        Member member = MemberFixture.of();
        GroupChallengeCategory category = GroupChallengeCategoryFixture.of("제로웨이스트");
        GroupChallenge challenge = GroupChallengeFixture.of(member, category);
        GroupChallengeParticipantRecord record = GroupChallengeParticipantRecordFixture.of(challenge, member);
        GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(record);
        verification.markVerified(ChallengeStatus.SUCCESS);

        when(verificationRepository.findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdAndCreatedAtBetween(
                eq(member.getId()), eq(challenge.getId()), any(), any()))
                .thenReturn(Optional.of(verification));

        // When
        ChallengeStatus result = service.getLatestStatus(member.getId(), challenge.getId());

        // Then
        assertThat(result).isEqualTo(ChallengeStatus.SUCCESS);
    }

    @Test
    @DisplayName("오늘 인증 기록이 없으면 NOT_SUBMITTED 상태를 반환한다")
    void getLatestStatus_GivenNoTodayVerification_ThenReturnsNotSubmitted() {
        // Given
        Long memberId = 1L;
        Long challengeId = 99L;

        when(verificationRepository.findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdAndCreatedAtBetween(
                eq(memberId), eq(challengeId), any(), any()))
                .thenReturn(Optional.empty());

        // When
        ChallengeStatus result = service.getLatestStatus(memberId, challengeId);

        // Then
        assertThat(result).isEqualTo(ChallengeStatus.NOT_SUBMITTED);
    }
}
