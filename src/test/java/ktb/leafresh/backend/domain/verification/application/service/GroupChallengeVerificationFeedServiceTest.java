package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.cache.VerificationStatCacheService;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationFeedQueryRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.LikeRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.GroupChallengeVerificationFeedSummaryDto;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import ktb.leafresh.backend.support.fixture.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChallengeVerificationFeedService 테스트")
class GroupChallengeVerificationFeedServiceTest {

    @Mock
    private GroupChallengeVerificationFeedQueryRepository feedQueryRepository;

    @Mock
    private VerificationStatCacheService verificationStatCacheService;

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private GroupChallengeVerificationFeedService feedService;

    private Member member;
    private GroupChallengeVerification verification;

    @BeforeEach
    void setUp() {
        member = MemberFixture.of();
        var category = GroupChallengeCategoryFixture.defaultCategory();
        var challenge = GroupChallengeFixture.of(member, category);
        var record = GroupChallengeParticipantRecordFixture.of(challenge, member);
        verification = GroupChallengeVerificationFixture.of(record);
        ReflectionTestUtils.setField(verification, "id", 1L);
        ReflectionTestUtils.setField(challenge, "id", 100L);
    }

    @Test
    @DisplayName("인증 피드 조회 시 - 로그인된 사용자는 좋아요 여부와 함께 페이징 결과를 반환한다")
    void getGroupChallengeVerifications_withValidInput_returnsPaginatedFeed() {
        // given
        Long loginMemberId = 10L;
        List<GroupChallengeVerification> verificationList = List.of(verification);
        Map<Object, Object> cachedStats = Map.of("viewCount", 30, "likeCount", 5, "commentCount", 2);
        Set<Long> likedIds = Set.of(1L);

        given(feedQueryRepository.findAllByFilter("ZERO_WASTE", null, null, 6))
                .willReturn(verificationList);
        given(verificationStatCacheService.getStats(1L)).willReturn(cachedStats);
        given(likeRepository.findLikedVerificationIdsByMemberId(loginMemberId, List.of(1L)))
                .willReturn(likedIds);

        // when
        CursorPaginationResult<GroupChallengeVerificationFeedSummaryDto> result =
                feedService.getGroupChallengeVerifications(null, null, 5, "ZERO_WASTE", loginMemberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.items()).hasSize(1);

        var dto = result.items().get(0);
        assertThat(dto.id()).isEqualTo(verification.getId());
        assertThat(dto.challengeId()).isEqualTo(verification.getParticipantRecord().getGroupChallenge().getId());
        assertThat(dto.nickname()).isEqualTo(member.getNickname());
        assertThat(dto.profileImageUrl()).isEqualTo(member.getImageUrl());
        assertThat(dto.verificationImageUrl()).isEqualTo(verification.getImageUrl());
        assertThat(dto.description()).isEqualTo(verification.getContent());
        assertThat(dto.category()).isEqualTo(verification.getParticipantRecord().getGroupChallenge().getCategory().getName());
        assertThat(dto.counts().view()).isEqualTo(30);
        assertThat(dto.counts().like()).isEqualTo(5);
        assertThat(dto.counts().comment()).isEqualTo(2);
        assertThat(dto.isLiked()).isTrue();
    }

    @Test
    @DisplayName("인증 피드 조회 시 - 로그인하지 않은 경우 좋아요 여부 없이 페이징 결과를 반환한다")
    void getGroupChallengeVerifications_withoutLogin_returnsFeedWithoutLikeInfo() {
        // given
        List<GroupChallengeVerification> verificationList = List.of(verification);
        Map<Object, Object> cachedStats = Map.of(); // 캐시 없는 경우 fallback

        given(feedQueryRepository.findAllByFilter("ZERO_WASTE", null, null, 6))
                .willReturn(verificationList);
        given(verificationStatCacheService.getStats(1L)).willReturn(cachedStats);

        // when
        CursorPaginationResult<GroupChallengeVerificationFeedSummaryDto> result =
                feedService.getGroupChallengeVerifications(null, null, 5, "ZERO_WASTE", null);

        // then
        assertThat(result).isNotNull();
        var dto = result.items().get(0);
        assertThat(dto.isLiked()).isFalse(); // 로그인하지 않은 경우 false
        assertThat(dto.counts().view()).isEqualTo(verification.getViewCount());
        assertThat(dto.counts().like()).isEqualTo(verification.getLikeCount());
        assertThat(dto.counts().comment()).isEqualTo(verification.getCommentCount());
    }
}
