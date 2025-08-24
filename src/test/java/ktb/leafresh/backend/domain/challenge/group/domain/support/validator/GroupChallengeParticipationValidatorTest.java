package ktb.leafresh.backend.domain.challenge.group.domain.support.validator;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeParticipantRecordRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.common.entity.enums.ParticipantStatus;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeParticipantRecordFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChallengeParticipationValidator 테스트")
class GroupChallengeParticipationValidatorTest {

  @Mock private GroupChallengeParticipantRecordRepository participantRepository;

  @InjectMocks private GroupChallengeParticipationValidator validator;

  @Test
  @DisplayName("이미 참여한 경우 예외 발생")
  void validateNotAlreadyParticipated_alreadyExists_throwsException() {
    // given
    Long challengeId = 1L;
    Long memberId = 2L;

    given(
            participantRepository.existsByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(
                challengeId, memberId))
        .willReturn(true);

    // expect
    assertThatThrownBy(() -> validator.validateNotAlreadyParticipated(challengeId, memberId))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ChallengeErrorCode.CHALLENGE_ALREADY_PARTICIPATED.getMessage());

    then(participantRepository)
        .should()
        .existsByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(challengeId, memberId);
  }

  @Test
  @DisplayName("참여 기록이 없으면 예외 없이 통과")
  void validateNotAlreadyParticipated_notExists_passes() {
    // given
    Long challengeId = 1L;
    Long memberId = 2L;

    given(
            participantRepository.existsByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(
                challengeId, memberId))
        .willReturn(false);

    // expect
    assertThatCode(() -> validator.validateNotAlreadyParticipated(challengeId, memberId))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("DROPPED 상태일 경우 예외 발생")
  void validateDroppable_dropped_throwsException() {
    validateDroppableWithInvalidStatus(ParticipantStatus.DROPPED);
  }

  @Test
  @DisplayName("FINISHED 상태일 경우 예외 발생")
  void validateDroppable_finished_throwsException() {
    validateDroppableWithInvalidStatus(ParticipantStatus.FINISHED);
  }

  @Test
  @DisplayName("BANNED 상태일 경우 예외 발생")
  void validateDroppable_banned_throwsException() {
    validateDroppableWithInvalidStatus(ParticipantStatus.BANNED);
  }

  @Test
  @DisplayName("ACTIVE 상태일 경우 예외 발생하지 않음")
  void validateDroppable_active_passes() {
    // given
    Member member = MemberFixture.of();
    GroupChallengeCategory category = GroupChallengeCategoryFixture.defaultCategory();
    GroupChallenge challenge = GroupChallengeFixture.of(member, category);
    GroupChallengeParticipantRecord record =
        GroupChallengeParticipantRecordFixture.of(challenge, member, ParticipantStatus.ACTIVE);

    // expect
    assertThatCode(() -> validator.validateDroppable(record)).doesNotThrowAnyException();
  }

  private void validateDroppableWithInvalidStatus(ParticipantStatus status) {
    // given
    Member member = MemberFixture.of();
    GroupChallengeCategory category = GroupChallengeCategoryFixture.defaultCategory();
    GroupChallenge challenge = GroupChallengeFixture.of(member, category);
    GroupChallengeParticipantRecord record =
        GroupChallengeParticipantRecordFixture.of(challenge, member, status);

    // expect
    assertThatThrownBy(() -> validator.validateDroppable(record))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ChallengeErrorCode.CHALLENGE_ALREADY_DROPPED.getMessage());
  }
}
