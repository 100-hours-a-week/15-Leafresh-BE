package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.service.GroupChallengeParticipantManager;
import ktb.leafresh.backend.domain.challenge.group.domain.support.policy.GroupChallengePromotionPolicy;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChallengeParticipationService 테스트")
class GroupChallengeParticipationServiceTest {

  @Mock private GroupChallengeParticipantManager participantManager;

  @Mock private GroupChallengePromotionPolicy promotionPolicy;

  @InjectMocks private GroupChallengeParticipationService participationService;

  private final Long memberId = 1L;
  private final Long challengeId = 100L;

  @Nested
  @DisplayName("participate()는")
  class Participate {

    @Test
    @DisplayName("정상적으로 챌린지에 참여할 수 있다.")
    void participateSuccessfully() {
      // given
      given(participantManager.participate(memberId, challengeId)).willReturn(challengeId);

      // when
      Long result = participationService.participate(memberId, challengeId);

      // then
      assertThat(result).isEqualTo(challengeId);
      then(participantManager).should().participate(memberId, challengeId);
    }

    @Test
    @DisplayName("참여 중 예외가 발생하면 CustomException으로 감싸서 던진다.")
    void throwsWrappedExceptionOnParticipateFailure() {
      // given
      RuntimeException cause = new RuntimeException("DB 오류");
      given(participantManager.participate(memberId, challengeId)).willThrow(cause);

      // when & then
      assertThatThrownBy(() -> participationService.participate(memberId, challengeId))
          .isInstanceOf(CustomException.class)
          .hasMessageContaining(
              ChallengeErrorCode.GROUP_CHALLENGE_PARTICIPATION_FAILED.getMessage());
    }
  }

  @Nested
  @DisplayName("drop()은")
  class Drop {

    @Test
    @DisplayName("정상적으로 참여를 취소하고 다음 대기자를 승격시킨다.")
    void dropSuccessfully() {
      // when
      participationService.drop(memberId, challengeId);

      // then
      then(participantManager).should().drop(memberId, challengeId);
      then(promotionPolicy).should().promoteNextWaitingParticipant(challengeId);
    }

    @Test
    @DisplayName("취소 중 예외가 발생하면 CustomException으로 감싸서 던진다.")
    void throwsWrappedExceptionOnDropFailure() {
      // given
      willThrow(new RuntimeException("예상치 못한 에러"))
          .given(participantManager)
          .drop(memberId, challengeId);

      // when & then
      assertThatThrownBy(() -> participationService.drop(memberId, challengeId))
          .isInstanceOf(CustomException.class)
          .hasMessageContaining(
              ChallengeErrorCode.GROUP_CHALLENGE_PARTICIPATION_FAILED.getMessage());
    }
  }
}
