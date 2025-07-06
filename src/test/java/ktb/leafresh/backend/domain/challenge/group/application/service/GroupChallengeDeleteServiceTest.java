package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeParticipantRecordRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static ktb.leafresh.backend.global.exception.ChallengeErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChallengeDeleteService 테스트")
class GroupChallengeDeleteServiceTest {

    @Mock
    private GroupChallengeRepository groupChallengeRepository;

    @Mock
    private GroupChallengeParticipantRecordRepository participantRecordRepository;

    @InjectMocks
    private GroupChallengeDeleteService groupChallengeDeleteService;

    @Nested
    @DisplayName("delete()는")
    class Delete {

        @Test
        @DisplayName("작성자가 참여자 없는 챌린지를 삭제하면 soft delete되고 challengeId를 반환한다.")
        void deleteGroupChallengeSuccessfully() {
            // given
            var member = MemberFixture.of();
            ReflectionTestUtils.setField(member, "id", 1L);

            var category = GroupChallengeCategoryFixture.defaultCategory();
            var challenge = GroupChallengeFixture.of(member, category);
            ReflectionTestUtils.setField(challenge, "id", 1L);

            given(groupChallengeRepository.findById(1L)).willReturn(Optional.of(challenge));
            given(participantRecordRepository.existsByGroupChallengeIdAndDeletedAtIsNull(1L)).willReturn(false);

            // when
            Long deletedId = groupChallengeDeleteService.delete(member.getId(), 1L);

            // then
            assertThat(deletedId).isEqualTo(1L);
            assertThat(challenge.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 챌린지를 삭제하려 하면 예외를 던진다.")
        void throwsExceptionIfChallengeNotFound() {
            // given
            given(groupChallengeRepository.findById(1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> groupChallengeDeleteService.delete(1L, 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(GROUP_CHALLENGE_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("이미 삭제된 챌린지를 다시 삭제하려 하면 예외를 던진다.")
        void throwsExceptionIfAlreadyDeleted() {
            // given
            var member = MemberFixture.of();
            ReflectionTestUtils.setField(member, "id", 2L);

            var category = GroupChallengeCategoryFixture.defaultCategory();
            var challenge = GroupChallengeFixture.of(member, category);
            challenge.softDelete();
            ReflectionTestUtils.setField(challenge, "id", 1L);

            given(groupChallengeRepository.findById(1L)).willReturn(Optional.of(challenge));

            // when & then
            assertThatThrownBy(() -> groupChallengeDeleteService.delete(member.getId(), 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CHALLENGE_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("작성자가 아닌 사용자가 삭제 요청하면 예외를 던진다.")
        void throwsExceptionIfNotWriter() {
            // given
            var writer = MemberFixture.of("writer@leafresh.com", "작성자");
            var other = MemberFixture.of("other@leafresh.com", "다른사람");

            ReflectionTestUtils.setField(writer, "id", 1L);
            ReflectionTestUtils.setField(other, "id", 2L);

            var category = GroupChallengeCategoryFixture.defaultCategory();
            var challenge = GroupChallengeFixture.of(writer, category);
            ReflectionTestUtils.setField(challenge, "id", 1L);

            given(groupChallengeRepository.findById(1L)).willReturn(Optional.of(challenge));

            // when & then
            assertThatThrownBy(() -> groupChallengeDeleteService.delete(other.getId(), 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CHALLENGE_ACCESS_DENIED.getMessage());
        }

        @Test
        @DisplayName("참여자가 있는 챌린지를 삭제하려 하면 예외를 던진다.")
        void throwsExceptionIfHasParticipants() {
            // given
            var member = MemberFixture.of();
            ReflectionTestUtils.setField(member, "id", 3L);

            var category = GroupChallengeCategoryFixture.defaultCategory();
            var challenge = GroupChallengeFixture.of(member, category);
            ReflectionTestUtils.setField(challenge, "id", 1L);

            given(groupChallengeRepository.findById(1L)).willReturn(Optional.of(challenge));
            given(participantRecordRepository.existsByGroupChallengeIdAndDeletedAtIsNull(1L)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> groupChallengeDeleteService.delete(member.getId(), 1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CHALLENGE_HAS_PARTICIPANTS_DELETE_NOT_ALLOWED.getMessage());
        }
    }
}
