package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeVerificationQueryRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeVerificationDetailResponseDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeVerificationSummaryDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.verification.application.dto.VerificationStatSnapshot;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.cache.VerificationStatCacheService;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.LikeRepository;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import ktb.leafresh.backend.global.util.redis.VerificationStatRedisLuaService;
import ktb.leafresh.backend.support.fixture.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

@DisplayName("GroupChallengeVerificationReadService 테스트")
@ExtendWith(MockitoExtension.class)
class GroupChallengeVerificationReadServiceTest {

    @Mock
    private GroupChallengeRepository groupChallengeRepository;

    @Mock
    private GroupChallengeVerificationRepository groupChallengeVerificationRepository;

    @Mock
    private GroupChallengeVerificationQueryRepository groupChallengeVerificationQueryRepository;

    @Mock
    private VerificationStatCacheService verificationStatCacheService;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private VerificationStatRedisLuaService verificationStatRedisLuaService;

    @InjectMocks
    private GroupChallengeVerificationReadService readService;

    private Member member;
    private GroupChallenge challenge;
    private GroupChallengeParticipantRecord record;
    private GroupChallengeVerification verification;

    @BeforeEach
    void setUp() {
        member = MemberFixture.of();
        GroupChallengeCategory category = GroupChallengeCategoryFixture.of("제로웨이스트");
        challenge = GroupChallengeFixture.of(member, category);
        record = GroupChallengeParticipantRecordFixture.of(challenge, member);
        verification = GroupChallengeVerificationFixture.of(record);
        ReflectionTestUtils.setField(verification, "id", 10L);
        ReflectionTestUtils.setField(verification, "updatedAt", LocalDateTime.of(2024, 1, 1, 10, 0));
    }

    @Test
    @DisplayName("인증 목록 조회 성공 - 로그인 사용자 있음")
    void getVerifications_withLoginMember_returnsPaginatedResult() {
        // given
        Long loginMemberId = 1L;
        List<GroupChallengeVerification> list = List.of(verification);
        Map<Object, Object> stats = Map.of("viewCount", "10", "likeCount", "2", "commentCount", "5");

        given(groupChallengeRepository.existsById(challenge.getId())).willReturn(true);
        given(groupChallengeVerificationQueryRepository.findByChallengeId(challenge.getId(), null, null, 11))
                .willReturn(list);
        given(verificationStatCacheService.getStats(verification.getId())).willReturn(stats);
        given(likeRepository.findLikedVerificationIdsByMemberId(loginMemberId, List.of(verification.getId())))
                .willReturn(Set.of(verification.getId()));

        // when
        CursorPaginationResult<GroupChallengeVerificationSummaryDto> result =
                readService.getVerifications(challenge.getId(), null, null, 10, loginMemberId);

        // then
        assertThat(result.items()).hasSize(1);
        GroupChallengeVerificationSummaryDto dto = result.items().get(0);
        assertThat(dto.id()).isEqualTo(verification.getId());
        assertThat(dto.counts().view()).isEqualTo(10);
        assertThat(dto.isLiked()).isTrue();
    }

    @Test
    @DisplayName("인증 목록 조회 실패 - 챌린지 없음")
    void getVerifications_whenChallengeNotFound_throwsException() {
        // given
        given(groupChallengeRepository.existsById(anyLong())).willReturn(false);

        // when & then
        assertThatThrownBy(() ->
                readService.getVerifications(999L, null, null, 10, null))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("인증 상세 조회 성공 - 비로그인 사용자")
    void getVerificationDetail_success_withNullLoginMember() {
        // given
        Long verificationId = verification.getId();
        Long challengeId = challenge.getId();
        Map<Object, Object> stats = Map.of("viewCount", "9", "likeCount", "1", "commentCount", "0");

        given(groupChallengeVerificationQueryRepository.findByChallengeIdAndId(challengeId, verificationId))
                .willReturn(Optional.of(verification));
        given(verificationStatCacheService.getStats(verificationId)).willReturn(stats);

        // when
        GroupChallengeVerificationDetailResponseDto dto =
                readService.getVerificationDetail(challengeId, verificationId, null);

        // then
        assertThat(dto.id()).isEqualTo(verificationId);
        assertThat(dto.counts().like()).isEqualTo(1);
        assertThat(dto.isLiked()).isFalse(); // 로그인 안 했으므로 false
    }

    @Test
    @DisplayName("단체 챌린지 인증 상세 조회 성공")
    void getVerificationDetail_success() {
        // given
        Long verificationId = verification.getId();
        Long challengeId = challenge.getId();
        Long loginMemberId = 1L;
        Map<Object, Object> stats = Map.of("viewCount", "11", "likeCount", "3", "commentCount", "1");

        given(groupChallengeVerificationQueryRepository.findByChallengeIdAndId(challengeId, verificationId))
                .willReturn(Optional.of(verification));
        given(verificationStatCacheService.getStats(verificationId)).willReturn(stats);
        given(likeRepository.findLikedVerificationIdsByMemberId(loginMemberId, List.of(verificationId)))
                .willReturn(Set.of(verificationId));

        // when
        GroupChallengeVerificationDetailResponseDto dto =
                readService.getVerificationDetail(challengeId, verificationId, loginMemberId);

        // then
        assertThat(dto.id()).isEqualTo(verificationId);
        assertThat(dto.counts().like()).isEqualTo(3);
        assertThat(dto.isLiked()).isTrue();
    }

    @Test
    @DisplayName("인증 상세 조회 실패 - 인증 없음")
    void getVerificationDetail_whenNotFound_throwsException() {
        // given
        given(groupChallengeVerificationQueryRepository.findByChallengeIdAndId(anyLong(), anyLong()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> readService.getVerificationDetail(1L, 2L, 3L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(VerificationErrorCode.VERIFICATION_DETAIL_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Redis 캐시 복구 - 캐시가 없는 경우")
    void recoverVerificationStatWithLock_shouldInitializeWhenCacheIsMissing() {
        // given
        Long verificationId = verification.getId();
        given(verificationStatCacheService.getStats(verificationId)).willReturn(Map.of());
        VerificationStatSnapshot snapshot = new VerificationStatSnapshot(
                verification.getId(),
                verification.getViewCount(),
                verification.getLikeCount(),
                verification.getCommentCount()
        );

        given(groupChallengeVerificationRepository.findStatById(verificationId))
                .willReturn(Optional.of(snapshot));

        // when
        readService.recoverVerificationStatWithLock(verificationId);

        // then
        verify(verificationStatCacheService).initializeVerificationStats(
                eq(verificationId),
                eq(verification.getViewCount()),
                eq(verification.getLikeCount()),
                eq(verification.getCommentCount())
        );
    }

    @Test
    @DisplayName("인증 상세 조회 성공 - 캐시가 없는 경우 복구 후 반환")
    void getVerificationDetail_withEmptyCache_recoversAndReturns() {
        // given
        Long verificationId = verification.getId();
        Long challengeId = challenge.getId();
        Long loginMemberId = 1L;
        Map<Object, Object> emptyStats = Map.of();
        Map<Object, Object> recoveredStats = Map.of("viewCount", "11", "likeCount", "3", "commentCount", "1");

        given(groupChallengeVerificationQueryRepository.findByChallengeIdAndId(challengeId, verificationId))
                .willReturn(Optional.of(verification));
        given(verificationStatCacheService.getStats(verificationId))
                .willReturn(emptyStats) // 첫 번째 호출: 캐시 없음
                .willReturn(recoveredStats); // 두 번째 호출: 복구 후 재조회
        given(likeRepository.findLikedVerificationIdsByMemberId(loginMemberId, List.of(verificationId)))
                .willReturn(Set.of(verificationId));

        // when
        GroupChallengeVerificationDetailResponseDto dto =
                readService.getVerificationDetail(challengeId, verificationId, loginMemberId);

        // then
        assertThat(dto.id()).isEqualTo(verificationId);
        assertThat(dto.counts().like()).isEqualTo(3);
        assertThat(dto.isLiked()).isTrue();
    }

    @Test
    @DisplayName("규약 조회 실패 - 존재하지 않는 챌린지")
    void getChallengeRules_whenNotFound_throwsException() {
        // given
        given(groupChallengeRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> readService.getChallengeRules(123L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_RULE_NOT_FOUND.getMessage());
    }
}
