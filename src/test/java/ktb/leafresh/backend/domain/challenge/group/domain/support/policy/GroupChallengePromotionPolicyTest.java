package ktb.leafresh.backend.domain.challenge.group.domain.support.policy;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeParticipantRecordRepository;
import ktb.leafresh.backend.global.common.entity.enums.ParticipantStatus;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeParticipantRecordFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChallengePromotionPolicy 테스트")
class GroupChallengePromotionPolicyTest {

  @Mock private GroupChallengeParticipantRecordRepository participantRepository;

  @InjectMocks private GroupChallengePromotionPolicy promotionPolicy;

  @Test
  @DisplayName("대기자 존재 시 첫 대기자를 ACTIVE로 승격하고 인원 수 증가")
  void promoteNextWaitingParticipant_withWaiting_promotesAndIncreasesCount() {
    // given
    Member member = MemberFixture.of();
    GroupChallengeCategory category = GroupChallengeCategoryFixture.defaultCategory();
    GroupChallenge challenge = GroupChallengeFixture.of(member, category);
    int originalCount = challenge.getCurrentParticipantCount();

    GroupChallengeParticipantRecord waitingParticipant =
        GroupChallengeParticipantRecordFixture.of(challenge, member, ParticipantStatus.WAITING);
    ReflectionTestUtils.setField(waitingParticipant, "groupChallenge", challenge);

    given(
            participantRepository.findFirstByGroupChallengeIdAndStatusOrderByCreatedAtAsc(
                challenge.getId(), ParticipantStatus.WAITING))
        .willReturn(Optional.of(waitingParticipant));

    // when
    promotionPolicy.promoteNextWaitingParticipant(challenge.getId());

    // then
    assertThat(waitingParticipant.getStatus()).isEqualTo(ParticipantStatus.ACTIVE);
    assertThat(challenge.getCurrentParticipantCount()).isEqualTo(originalCount + 1);

    then(participantRepository)
        .should(times(1))
        .findFirstByGroupChallengeIdAndStatusOrderByCreatedAtAsc(
            challenge.getId(), ParticipantStatus.WAITING);
  }

  @Test
  @DisplayName("대기자가 없으면 아무 동작도 하지 않음")
  void promoteNextWaitingParticipant_withoutWaiting_doesNothing() {
    // given
    Long challengeId = 1L;
    given(
            participantRepository.findFirstByGroupChallengeIdAndStatusOrderByCreatedAtAsc(
                challengeId, ParticipantStatus.WAITING))
        .willReturn(Optional.empty());

    // when
    promotionPolicy.promoteNextWaitingParticipant(challengeId);

    // then
    then(participantRepository)
        .should(times(1))
        .findFirstByGroupChallengeIdAndStatusOrderByCreatedAtAsc(
            challengeId, ParticipantStatus.WAITING);
  }
}
