package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.service.GroupChallengeVerificationHistoryCalculator;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeParticipantRecordRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeVerificationQueryRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeVerificationHistoryResponseDto;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.support.fixture.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChallengeVerificationHistoryService 테스트")
class GroupChallengeVerificationHistoryServiceTest {

    @Mock
    private GroupChallengeRepository challengeRepository;

    @Mock
    private GroupChallengeParticipantRecordRepository recordRepository;

    @Mock
    private GroupChallengeVerificationQueryRepository verificationRepository;

    @Mock
    private GroupChallengeVerificationHistoryCalculator calculator;

    @InjectMocks
    private GroupChallengeVerificationHistoryService historyService;

    @Test
    @DisplayName("정상 조회 시 검증 결과를 반환한다.")
    void getVerificationHistory_success() {
        // given
        Long memberId = 1L;
        Long challengeId = 10L;

        var member = MemberFixture.of();
        var category = GroupChallengeCategoryFixture.defaultCategory();
        var challenge = GroupChallengeFixture.of(member, category);
        var record = GroupChallengeParticipantRecordFixture.of(challenge, member);
        var verifications = List.of(GroupChallengeVerificationFixture.of(record));

        var expectedResponse = GroupChallengeVerificationHistoryResponseDto.builder()
                .id(challengeId)
                .title("제로웨이스트 챌린지")
                .achievement(GroupChallengeVerificationHistoryResponseDto.AchievementDto.builder()
                        .success(1)
                        .failure(0)
                        .remaining(6)
                        .build())
                .verifications(List.of())
                .todayStatus("PENDING_APPROVAL")
                .build();

        given(challengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));
        given(recordRepository.findByMemberIdAndGroupChallengeIdAndDeletedAtIsNull(memberId, challengeId)).willReturn(Optional.of(record));
        given(verificationRepository.findByParticipantRecordId(record.getId())).willReturn(verifications);
        given(calculator.calculate(challenge, record, verifications)).willReturn(expectedResponse);

        // when
        var result = historyService.getVerificationHistory(memberId, challengeId);

        // then
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("존재하지 않는 챌린지 ID라면 예외를 던진다.")
    void getVerificationHistory_challengeNotFound() {
        // given
        Long memberId = 1L, challengeId = 999L;
        given(challengeRepository.findById(challengeId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> historyService.getVerificationHistory(memberId, challengeId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_VERIFICATION_CHALLENGE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("참여 기록이 없다면 예외를 던진다.")
    void getVerificationHistory_participantNotFound() {
        // given
        Long memberId = 1L, challengeId = 999L;
        var member = MemberFixture.of();
        var category = GroupChallengeCategoryFixture.defaultCategory();
        var challenge = GroupChallengeFixture.of(member, category);

        given(challengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));
        given(recordRepository.findByMemberIdAndGroupChallengeIdAndDeletedAtIsNull(memberId, challengeId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> historyService.getVerificationHistory(memberId, challengeId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_VERIFICATION_ACCESS_DENIED.getMessage());
    }

    @Test
    @DisplayName("예기치 못한 예외가 발생하면 공통 실패 코드로 예외를 던진다.")
    void getVerificationHistory_unknownError() {
        // given
        Long memberId = 1L, challengeId = 10L;
        given(challengeRepository.findById(challengeId)).willThrow(new RuntimeException("DB ERROR"));

        // when & then
        assertThatThrownBy(() -> historyService.getVerificationHistory(memberId, challengeId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_VERIFICATION_READ_FAILED.getMessage());
    }
}
