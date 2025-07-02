//package ktb.leafresh.backend.domain.member.application.service;
//
//import ktb.leafresh.backend.domain.member.domain.entity.Badge;
//import ktb.leafresh.backend.domain.member.domain.entity.Member;
//import ktb.leafresh.backend.domain.member.domain.entity.MemberBadge;
//import ktb.leafresh.backend.domain.member.domain.service.policy.BadgeGrantPolicy;
//import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
//import ktb.leafresh.backend.support.fixture.BadgeFixture;
//import ktb.leafresh.backend.support.fixture.MemberFixture;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class BadgeGrantManagerTest {
//
//    @Mock
//    private MemberBadgeRepository memberBadgeRepository;
//
//    @Mock
//    private BadgeGrantPolicy policy1;
//
//    @Mock
//    private BadgeGrantPolicy policy2;
//
//    @InjectMocks
//    private BadgeGrantManager badgeGrantManager;
//
//    @Test
//    @DisplayName("evaluateAllAndGrant_모든정책에서새뱃지반환_저장됨")
//    void evaluateAllAndGrant_givenMultiplePoliciesWithNewBadges_thenSaveAll() {
//        // given
//        Member member = MemberFixture.of();
//        Badge badge1 = BadgeFixture.of("첫 발자국");
//        Badge badge2 = BadgeFixture.of("지속가능 파이터");
//
//        when(policy1.evaluateAndGetNewBadges(member)).thenReturn(List.of(badge1));
//        when(policy2.evaluateAndGetNewBadges(member)).thenReturn(List.of(badge2));
//        when(memberBadgeRepository.existsByMemberAndBadge(member, badge1)).thenReturn(false);
//        when(memberBadgeRepository.existsByMemberAndBadge(member, badge2)).thenReturn(false);
//
//        ArgumentCaptor<MemberBadge> captor = ArgumentCaptor.forClass(MemberBadge.class);
//
//        // when
//        badgeGrantManager.evaluateAllAndGrant(member);
//
//        // then
//        verify(memberBadgeRepository, times(2)).save(captor.capture());
//        List<MemberBadge> saved = captor.getAllValues();
//
//        assertThat(saved)
//                .extracting(mb -> mb.getBadge().getName())
//                .containsExactlyInAnyOrder(badge1.getName(), badge2.getName());
//    }
//
//    @Test
//    @DisplayName("evaluateAllAndGrant_이미보유한뱃지_저장안함")
//    void evaluateAllAndGrant_givenAlreadyOwnedBadge_thenSkipSave() {
//        // given
//        Member member = MemberFixture.of();
//        Badge badge = BadgeFixture.of("첫 발자국");
//
//        when(policy1.evaluateAndGetNewBadges(member)).thenReturn(List.of(badge));
//        when(memberBadgeRepository.existsByMemberAndBadge(member, badge)).thenReturn(true);
//
//        // when
//        badgeGrantManager.evaluateAllAndGrant(member);
//
//        // then
//        verify(memberBadgeRepository, never()).save(any());
//    }
//
//    @Test
//    @DisplayName("evaluateAllAndGrant_정책반환값없음_저장안함")
//    void evaluateAllAndGrant_givenNoBadgeFromPolicies_thenDoNotSave() {
//        // given
//        Member member = MemberFixture.of();
//
//        when(policy1.evaluateAndGetNewBadges(member)).thenReturn(List.of());
//        when(policy2.evaluateAndGetNewBadges(member)).thenReturn(List.of());
//
//        // when
//        badgeGrantManager.evaluateAllAndGrant(member);
//
//        // then
//        verify(memberBadgeRepository, never()).save(any());
//    }
//}
