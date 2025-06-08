package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.cache.VerificationStatCacheService;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationFeedQueryRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.LikeRepository;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeParticipantRecordFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeVerificationFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class GroupChallengeVerificationFeedServiceTest {

    private GroupChallengeVerificationFeedQueryRepository feedQueryRepository;
    private VerificationStatCacheService verificationStatCacheService;
    private LikeRepository likeRepository;
    private GroupChallengeVerificationFeedService service;

    @BeforeEach
    void setUp() {
        feedQueryRepository = mock(GroupChallengeVerificationFeedQueryRepository.class);
        verificationStatCacheService = mock(VerificationStatCacheService.class);
        likeRepository = mock(LikeRepository.class);
        service = new GroupChallengeVerificationFeedService(feedQueryRepository, verificationStatCacheService, likeRepository);
    }

    @Test
    @DisplayName("카테고리 필터와 로그인 회원 정보를 기준으로 인증 피드를 조회한다")
    void getGroupChallengeVerifications_GivenCategoryAndLoginMemberId_ThenReturnsVerificationFeed() {
        // Given
        Long loginMemberId = 1L;
        String category = "ZERO_WASTE";
        int size = 10;
        Long cursorId = null;
        String cursorTimestamp = null;

        Member member = MemberFixture.of();
        GroupChallengeCategory groupChallengeCategory = GroupChallengeCategoryFixture.of(category);
        GroupChallenge challenge = GroupChallengeFixture.of(member, groupChallengeCategory);
        GroupChallengeParticipantRecord record = GroupChallengeParticipantRecordFixture.of(challenge, member);
        GroupChallengeVerification verification = GroupChallengeVerificationFixture.of(record);

        given(feedQueryRepository.findAllByFilter(category, cursorId, cursorTimestamp, size + 1))
                .willReturn(List.of(verification));

        given(verificationStatCacheService.getStats(verification.getId()))
                .willReturn(Map.of(
                        "viewCount", "10",
                        "likeCount", "5",
                        "commentCount", "3"
                ));

        given(likeRepository.findLikedVerificationIdsByMemberId(loginMemberId, List.of(verification.getId())))
                .willReturn(Set.of(verification.getId()));

        // When
        var result = service.getGroupChallengeVerifications(cursorId, cursorTimestamp, size, category, loginMemberId);

        // Then
        assertThat(result.items()).hasSize(1);
        var dto = result.items().get(0);

        assertThat(dto.id()).isEqualTo(verification.getId());
        assertThat(dto.challengeId()).isEqualTo(challenge.getId());
        assertThat(dto.nickname()).isEqualTo(member.getNickname());
        assertThat(dto.profileImageUrl()).isEqualTo(member.getImageUrl());
        assertThat(dto.verificationImageUrl()).isEqualTo(verification.getImageUrl());
        assertThat(dto.description()).isEqualTo(verification.getContent());
        assertThat(dto.category()).isEqualTo(category);
        assertThat(dto.isLiked()).isTrue();
        assertThat(dto.counts().view()).isEqualTo(10);
        assertThat(dto.counts().like()).isEqualTo(5);
        assertThat(dto.counts().comment()).isEqualTo(3);
    }
}
