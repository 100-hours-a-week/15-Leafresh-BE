package ktb.leafresh.backend.domain.member.application.service.policy;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.support.fixture.BadgeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventChallengeBadgePolicy 테스트")
class EventChallengeBadgePolicyTest {

    @Mock
    private GroupChallengeVerificationRepository groupVerificationRepository;

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private MemberBadgeRepository memberBadgeRepository;

    @InjectMocks
    private EventChallengeBadgePolicy policy;

    @Test
    @DisplayName("이벤트 인증 3회 이상 성공 시 뱃지가 지급된다")
    void evaluateAndGetNewBadges_givenValidEventSuccessCountOver3_thenGrantBadge() {
        // given
        Member member = MemberFixture.of();
        String eventTitle = "세계 습지의 날";
        String badgeName = "습지 전도사";
        Badge badge = BadgeFixture.of(badgeName);

        when(groupVerificationRepository.findDistinctEventTitlesWithEventFlagTrue())
                .thenReturn(List.of(eventTitle));
        when(groupVerificationRepository.countByMemberIdAndEventTitleAndStatus(
                member.getId(), eventTitle, ChallengeStatus.SUCCESS)).thenReturn(3L);
        when(badgeRepository.findByName(badgeName)).thenReturn(Optional.of(badge));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge)).thenReturn(false);

        // when
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // then
        assertThat(result).containsExactly(badge);
    }

    @Test
    @DisplayName("이미 보유한 뱃지는 새로 지급되지 않는다")
    void evaluateAndGetNewBadges_givenAlreadyOwnedBadge_thenDoNotGrantAgain() {
        // given
        Member member = MemberFixture.of();
        String eventTitle = "세계 습지의 날";
        String badgeName = "습지 전도사";
        Badge badge = BadgeFixture.of(badgeName);

        when(groupVerificationRepository.findDistinctEventTitlesWithEventFlagTrue())
                .thenReturn(List.of(eventTitle));
        when(groupVerificationRepository.countByMemberIdAndEventTitleAndStatus(
                member.getId(), eventTitle, ChallengeStatus.SUCCESS)).thenReturn(3L);
        when(badgeRepository.findByName(badgeName)).thenReturn(Optional.of(badge));
        when(memberBadgeRepository.existsByMemberAndBadge(member, badge)).thenReturn(true);

        // when
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("매핑되지 않은 이벤트명은 무시된다")
    void evaluateAndGetNewBadges_givenUnmappedEventTitle_thenSkip() {
        // given
        Member member = MemberFixture.of();
        String unknownEventTitle = "무명 이벤트";

        when(groupVerificationRepository.findDistinctEventTitlesWithEventFlagTrue())
                .thenReturn(List.of(unknownEventTitle));
        when(groupVerificationRepository.countByMemberIdAndEventTitleAndStatus(
                member.getId(), unknownEventTitle, ChallengeStatus.SUCCESS)).thenReturn(5L);

        // when
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공 인증 횟수가 부족한 경우 뱃지가 지급되지 않는다")
    void evaluateAndGetNewBadges_givenSuccessCountLessThan3_thenNoBadgeGranted() {
        // given
        Member member = MemberFixture.of();
        String eventTitle = "세계 습지의 날";

        when(groupVerificationRepository.findDistinctEventTitlesWithEventFlagTrue())
                .thenReturn(List.of(eventTitle));
        when(groupVerificationRepository.countByMemberIdAndEventTitleAndStatus(
                member.getId(), eventTitle, ChallengeStatus.SUCCESS)).thenReturn(2L);

        // when
        List<Badge> result = policy.evaluateAndGetNewBadges(member);

        // then
        assertThat(result).isEmpty();
    }
}
