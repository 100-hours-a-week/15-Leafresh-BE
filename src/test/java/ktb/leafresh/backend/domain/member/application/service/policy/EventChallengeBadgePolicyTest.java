package ktb.leafresh.backend.domain.member.application.service.policy;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.support.fixture.BadgeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EventChallengeBadgePolicyTest {

    private GroupChallengeVerificationRepository groupVerificationRepository;
    private BadgeRepository badgeRepository;
    private MemberBadgeRepository memberBadgeRepository;
    private EventChallengeBadgePolicy policy;

    @BeforeEach
    void setUp() {
        groupVerificationRepository = mock(GroupChallengeVerificationRepository.class);
        badgeRepository = mock(BadgeRepository.class);
        memberBadgeRepository = mock(MemberBadgeRepository.class);
        policy = new EventChallengeBadgePolicy(groupVerificationRepository, badgeRepository, memberBadgeRepository);
    }

    @Test
    @DisplayName("이벤트 인증 3회 이상 성공 시 뱃지가 지급된다")
    void evaluateAndGetNewBadges_GivenEventSuccessOver3_ThenGrantBadge() {
        // Given
        Member member = MemberFixture.of();
        String eventTitle = "세계 습지의 날";
        String badgeName = "습지 전도사";
        Badge badge = BadgeFixture.of(1L, badgeName);

        when(groupVerificationRepository.findDistinctEventTitlesWithEventFlagTrue())
                .thenReturn(List.of(eventTitle));

        when(groupVerificationRepository.countByMemberIdAndEventTitleAndStatus(
                member.getId(), eventTitle, ChallengeStatus.SUCCESS))
                .thenReturn(3L);

        when(badgeRepository.findByName(badgeName)).thenReturn(Optional.of(badge));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge)).thenReturn(false);

        // When
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // Then
        assertThat(result).containsExactly(badge);
    }

    @Test
    @DisplayName("뱃지를 이미 보유한 경우에는 새로 지급되지 않는다")
    void evaluateAndGetNewBadges_AlreadyOwnedBadge_ThenNoNewBadge() {
        // Given
        Member member = MemberFixture.of();
        String eventTitle = "세계 습지의 날";
        String badgeName = "습지 전도사";
        Badge badge = BadgeFixture.of(2L, badgeName);

        when(groupVerificationRepository.findDistinctEventTitlesWithEventFlagTrue())
                .thenReturn(List.of(eventTitle));

        when(groupVerificationRepository.countByMemberIdAndEventTitleAndStatus(
                member.getId(), eventTitle, ChallengeStatus.SUCCESS))
                .thenReturn(3L);

        when(badgeRepository.findByName(badgeName)).thenReturn(Optional.of(badge));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge)).thenReturn(true);

        // When
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이벤트명이 뱃지명과 매핑되지 않으면 무시된다")
    void evaluateAndGetNewBadges_UnmappedEventTitle_ThenSkip() {
        // Given
        Member member = MemberFixture.of();
        String unknownEvent = "무명 이벤트";

        when(groupVerificationRepository.findDistinctEventTitlesWithEventFlagTrue())
                .thenReturn(List.of(unknownEvent));

        when(groupVerificationRepository.countByMemberIdAndEventTitleAndStatus(
                member.getId(), unknownEvent, ChallengeStatus.SUCCESS))
                .thenReturn(5L); // 조건은 만족하지만 매핑 없음

        // When
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공 인증 횟수가 부족하면 뱃지를 지급하지 않는다")
    void evaluateAndGetNewBadges_LessThan3Success_ThenNoBadge() {
        // Given
        Member member = MemberFixture.of();
        String eventTitle = "세계 습지의 날";

        when(groupVerificationRepository.findDistinctEventTitlesWithEventFlagTrue())
                .thenReturn(List.of(eventTitle));

        when(groupVerificationRepository.countByMemberIdAndEventTitleAndStatus(
                member.getId(), eventTitle, ChallengeStatus.SUCCESS))
                .thenReturn(2L); // 조건 미달

        // When
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // Then
        assertThat(result).isEmpty();
    }
}
