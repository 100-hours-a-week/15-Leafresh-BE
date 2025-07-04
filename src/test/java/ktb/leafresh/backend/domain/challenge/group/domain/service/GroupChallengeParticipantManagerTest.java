package ktb.leafresh.backend.domain.challenge.group.domain.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.domain.support.validator.GroupChallengeParticipationValidator;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeParticipantRecordRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.global.common.entity.enums.ParticipantStatus;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeParticipantRecordFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChallengeParticipantManager 단위 테스트")
class GroupChallengeParticipantManagerTest {

    @Mock
    private GroupChallengeRepository groupChallengeRepository;

    @Mock
    private GroupChallengeParticipantRecordRepository participantRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GroupChallengeParticipationValidator validator;

    @InjectMocks
    private GroupChallengeParticipantManager participantManager;

    private Member member;
    private GroupChallenge challenge;

    @BeforeEach
    void setUp() {
        member = MemberFixture.of();
        GroupChallengeCategory category = GroupChallengeCategoryFixture.defaultCategory();
        challenge = GroupChallengeFixture.of(member, category);
    }

    @Test
    @DisplayName("참여 성공 - 모집 미달 상태")
    void participate_withValidInput_returnsId() {
        // given
        Long memberId = 1L;
        Long challengeId = 100L;

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(groupChallengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));

        // 모집 미달 → ACTIVE 상태 → 참여자 수 증가 기대
        given(participantRepository.save(any())).willAnswer(inv -> {
            GroupChallengeParticipantRecord saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 500L);
            return saved;
        });

        // when
        Long id = participantManager.participate(memberId, challengeId);

        // then
        assertThat(id).isEqualTo(500L);
        assertThat(challenge.getCurrentParticipantCount()).isEqualTo(11);
        then(validator).should().validateNotAlreadyParticipated(challengeId, memberId);
    }

    @Test
    @DisplayName("참여 성공 - 모집 완료 상태")
    void participate_whenFull_setsWaitingStatus() {
        // given
        ReflectionTestUtils.setField(challenge, "currentParticipantCount", challenge.getMaxParticipantCount());

        Long memberId = 1L;
        Long challengeId = 100L;

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(groupChallengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));

        // when
        participantManager.participate(memberId, challengeId);

        // then
        assertThat(challenge.getCurrentParticipantCount()).isEqualTo(100); // 변함 없음
        then(participantRepository).should().save(argThat(record ->
                record.getStatus() == ParticipantStatus.WAITING));
    }

    @Test
    @DisplayName("참여 실패 - 존재하지 않는 회원")
    void participate_withInvalidMember_throwsException() {
        // given
        Long invalidMemberId = 999L;
        given(memberRepository.findById(invalidMemberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> participantManager.participate(invalidMemberId, 1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("참여 실패 - 존재하지 않는 챌린지")
    void participate_withInvalidChallenge_throwsException() {
        // given
        Long challengeId = 999L;
        given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));
        given(groupChallengeRepository.findById(challengeId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> participantManager.participate(1L, challengeId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("탈퇴 성공 - ACTIVE 상태")
    void drop_withActiveParticipant_decreasesCountAndUpdatesStatus() {
        // given
        Long memberId = 1L;
        Long challengeId = 10L;

        GroupChallengeParticipantRecord record = GroupChallengeParticipantRecordFixture.of(challenge, member);
        given(groupChallengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));
        given(participantRepository.findByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(challengeId, memberId))
                .willReturn(Optional.of(record));

        // when
        participantManager.drop(memberId, challengeId);

        // then
        assertThat(challenge.getCurrentParticipantCount()).isEqualTo(9);
        assertThat(record.getStatus()).isEqualTo(ParticipantStatus.DROPPED);
        then(validator).should().validateDroppable(record);
    }

    @Test
    @DisplayName("탈퇴 실패 - 존재하지 않는 챌린지")
    void drop_withInvalidChallenge_throwsException() {
        given(groupChallengeRepository.findById(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> participantManager.drop(1L, 2L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("탈퇴 실패 - 이미 삭제된 챌린지")
    void drop_deletedChallenge_throwsException() {
        challenge.softDelete(); // deletedAt 설정
        given(groupChallengeRepository.findById(anyLong())).willReturn(Optional.of(challenge));

        assertThatThrownBy(() -> participantManager.drop(1L, 2L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_ALREADY_DELETED.getMessage());
    }

    @Test
    @DisplayName("탈퇴 실패 - 참가 기록 없음")
    void drop_participationNotFound_throwsException() {
        given(groupChallengeRepository.findById(anyLong())).willReturn(Optional.of(challenge));
        given(participantRepository.findByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(anyLong(), anyLong()))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> participantManager.drop(1L, 2L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_PARTICIPATION_NOT_FOUND.getMessage());
    }
}
