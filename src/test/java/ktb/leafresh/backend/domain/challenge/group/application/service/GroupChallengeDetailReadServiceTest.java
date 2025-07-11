package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeDetailResponseDto;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.support.fixture.*;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.exception.CustomException;
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

import java.util.List;
import java.util.Optional;

import static ktb.leafresh.backend.global.exception.ChallengeErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChallengeDetailReadService 테스트")
class GroupChallengeDetailReadServiceTest {

    @Mock
    private GroupChallengeRepository groupChallengeRepository;

    @Mock
    private GroupChallengeVerificationRepository verificationRepository;

    @InjectMocks private GroupChallengeDetailReadService groupChallengeDetailReadService;

    @Nested
    @DisplayName("getChallengeDetail()은")
    class GetChallengeDetail {

        @Test
        @DisplayName("정상적으로 상세 정보를 조회하고 응답 객체를 반환한다.")
        void returnChallengeDetailSuccessfully() {
            // given
            Member member = MemberFixture.of();
            ReflectionTestUtils.setField(member, "id", 1L);

            GroupChallenge challenge = GroupChallengeFixture.of(member, GroupChallengeCategoryFixture.defaultCategory());
            ReflectionTestUtils.setField(challenge, "id", 100L);

            GroupChallengeParticipantRecord record = GroupChallengeParticipantRecordFixture.of(challenge, member);
            GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(record);
            List<GroupChallengeVerification> verifications = List.of(verification);

            given(groupChallengeRepository.findById(100L)).willReturn(Optional.of(challenge));
            given(verificationRepository.findTop9ByParticipantRecord_GroupChallenge_IdOrderByCreatedAtDesc(100L))
                    .willReturn(verifications);
            given(verificationRepository.findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdOrderByCreatedAtDesc(1L, 100L))
                    .willReturn(Optional.of(verification));

            // when
            GroupChallengeDetailResponseDto result = groupChallengeDetailReadService.getChallengeDetail(1L, 100L);

            // then
            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(ChallengeStatus.SUCCESS);
            assertThat(result.verificationImages()).containsExactly("https://dummy.image/verify.jpg");
            assertThat(result.exampleImages()).hasSize(challenge.getExampleImages().size());
        }

        @Test
        @DisplayName("존재하지 않는 챌린지를 조회할 경우 예외를 던진다.")
        void throwsExceptionWhenChallengeNotFound() {
            // given
            given(groupChallengeRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> groupChallengeDetailReadService.getChallengeDetail(1L, 999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(GROUP_CHALLENGE_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("이미 삭제된 챌린지를 조회할 경우 예외를 던진다.")
        void throwsExceptionWhenAlreadyDeleted() {
            // given
            Member member = MemberFixture.of();
            ReflectionTestUtils.setField(member, "id", 2L);

            GroupChallenge challenge = GroupChallengeFixture.of(member, GroupChallengeCategoryFixture.defaultCategory());
            challenge.softDelete(); // 삭제 처리
            ReflectionTestUtils.setField(challenge, "id", 300L);

            given(groupChallengeRepository.findById(300L)).willReturn(Optional.of(challenge));

            // when & then
            assertThatThrownBy(() -> groupChallengeDetailReadService.getChallengeDetail(2L, 300L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(GROUP_CHALLENGE_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("비로그인 사용자는 status가 NOT_SUBMITTED로 반환된다.")
        void returnNotSubmittedForAnonymousUser() {
            // given
            Member member = MemberFixture.of();
            GroupChallenge challenge = GroupChallengeFixture.of(member, GroupChallengeCategoryFixture.defaultCategory());
            ReflectionTestUtils.setField(challenge, "id", 400L);

            given(groupChallengeRepository.findById(400L)).willReturn(Optional.of(challenge));
            given(verificationRepository.findTop9ByParticipantRecord_GroupChallenge_IdOrderByCreatedAtDesc(400L))
                    .willReturn(List.of());

            // when
            GroupChallengeDetailResponseDto result = groupChallengeDetailReadService.getChallengeDetail(null, 400L);

            // then
            assertThat(result.status()).isEqualTo(ChallengeStatus.NOT_SUBMITTED);
        }
    }
}
